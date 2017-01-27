/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package burp.userinterface;

import burp.IBurpExtenderCallbacks;
import burp.IExtensionHelpers;
import burp.IHttpRequestResponse;
import burp.IHttpService;
import burp.IMessageEditor;
import burp.IMessageEditorController;
import burp.IRequestInfo;
import burp.util.IHttpServiceImpl;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedList;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Joaquin R. Martinez
 */
public class UInterface extends JPanel implements ActionListener{

    private DefaultTableModel requestsModel;
    private IMessageEditor msgeditorRequest, msgeditorResponse, msgeditoModifiedRequest,
            msgeditorModifiedResponse;
    //private JCheckBox chb_automaticAddToList;
    private LinkedList<IHttpRequestResponse> requestsList, modifiedRequestsList;
    private IExtensionHelpers helpers;
    private int contRequests;
    private JTable requestsTable;
    private JButton cleanButton;
    private JTextField hostField;

    public UInterface(IBurpExtenderCallbacks ibec) {
        //super(new BorderLayout(10,10));
        this.setBackground(Color.WHITE);
        BoxLayout boxLayout = new BoxLayout(this, BoxLayout.Y_AXIS);
        this.setLayout(boxLayout);
        this.helpers = ibec.getHelpers();
        this.requestsList = new LinkedList<>();
        this.modifiedRequestsList = new LinkedList<>();
        this.cleanButton = new JButton("clear table");
        this.cleanButton.addActionListener(this);
        //this.btn_same_url_method = new JButton("Delete duplicated items")
        //this.btn_same_url_method.addActionListener(this);
        contRequests = 1;
        hostField = new JTextField(20);
        //chb_automaticAddToList = new JCheckBox("Add request to list (If sends CSRF Tokens)");
        this.requestsModel = new DefaultTableModel(new String[]{"#id", "method", "url"}, 0);
        this.msgeditorRequest = ibec.createMessageEditor(new IMessageEditorController() {
            @Override
            public IHttpService getHttpService() {
                return new IHttpServiceImpl(helpers.analyzeRequest(msgeditorRequest.getMessage()).getUrl());
            }

            @Override
            public byte[] getRequest() {
                return msgeditorRequest.getMessage();
            }

            @Override
            public byte[] getResponse() {
                return null;
            }
        }, false);
        this.msgeditorResponse = ibec.createMessageEditor(new IMessageEditorController() {
            @Override
            public IHttpService getHttpService() {
                return new IHttpServiceImpl(helpers.analyzeRequest(msgeditorResponse.getMessage()).getUrl());
            }

            @Override
            public byte[] getRequest() {
                return null;
            }

            @Override
            public byte[] getResponse() {
                return msgeditorResponse.getMessage();
            }
        }, false);
        this.msgeditoModifiedRequest = ibec.createMessageEditor(new IMessageEditorController() {
            @Override
            public IHttpService getHttpService() {
                return new IHttpServiceImpl(helpers.analyzeRequest(msgeditoModifiedRequest.getMessage()).getUrl());
            }

            @Override
            public byte[] getRequest() {
                return msgeditoModifiedRequest.getMessage();
            }

            @Override
            public byte[] getResponse() {
                return null;
            }
        }, false);
        this.msgeditorModifiedResponse = ibec.createMessageEditor(new IMessageEditorController() {
            @Override
            public IHttpService getHttpService() {
                return new IHttpServiceImpl(helpers.analyzeRequest(msgeditorModifiedResponse.getMessage()).getUrl());
            }
            @Override
            public byte[] getRequest() {
                return null;
            }
            @Override
            public byte[] getResponse() {
                return msgeditorModifiedResponse.getMessage();
            }
        }, false);
        
        requestsTable = new JTable();
        //tbl_requests.setEnabled(false);
        requestsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        requestsTable.addMouseListener(new MouseAdapter() { /*evento para cambiar de Request-Response en el Editor de mensages (MessageEditor)*/

            @Override
            public void mouseClicked(MouseEvent e) {
                int selectedRow = requestsTable.getSelectedRow();
                if (selectedRow != -1) {
                    IHttpRequestResponse httpReqResU1 = requestsList.get(selectedRow);
                    try {
                        msgeditorRequest.setMessage(httpReqResU1.getRequest(), true);
                        msgeditorResponse.setMessage(httpReqResU1.getResponse(), false);
                        
                        IHttpRequestResponse httpReqResU2 = modifiedRequestsList.get(selectedRow);
                        msgeditoModifiedRequest.setMessage(httpReqResU2.getRequest(), true);
                        msgeditorModifiedResponse.setMessage(httpReqResU2.getResponse(), false);

                    } catch (Exception ex) { }
                }
            }
        });
        requestsTable.setModel(this.requestsModel);
        JPanel pnl_izquierdo = new JPanel();
        BoxLayout box = new BoxLayout(pnl_izquierdo, BoxLayout.Y_AXIS);
        pnl_izquierdo.setLayout(box);
        JScrollPane scl_tblRequests = new JScrollPane();
        scl_tblRequests.setViewportView(requestsTable);
        Border brd_pnlIdors = new TitledBorder(new LineBorder(Color.BLACK), "Suspicious List");
        scl_tblRequests.setBorder(brd_pnlIdors);
        pnl_izquierdo.add(scl_tblRequests);
        
        JPanel pnl_bottom = new JPanel();
        pnl_bottom.add(cleanButton);
        pnl_bottom.add(new JLabel("Host:"));
        pnl_bottom.add(hostField);
        pnl_izquierdo.add(pnl_bottom);
        //crear tab que contiene los del usuario 1 y 2, ademas los del CSRF
        JTabbedPane tab_principal = new JTabbedPane();
        //crear panel preview HTTP
        //crear panel request preview
        JTabbedPane tab_original = new JTabbedPane();
        JTabbedPane tab_modified = new JTabbedPane();
        //agregar al tab 2 los requestst/responeses del usuario 2
        tab_original.add("Request", this.msgeditorRequest.getComponent());
        tab_original.add("Response", this.msgeditorResponse.getComponent());
        //agregar al tab 2 los requestst/responeses del usuario 2
        tab_modified.add("Request", this.msgeditoModifiedRequest.getComponent());
        tab_modified.add("Response", this.msgeditorModifiedResponse.getComponent());
        //agregar al tab de csrf el request/response correspondiente
        //agragar los tabs del usuario 1 y 2 y el de CSRF al tab principal
        tab_principal.add("Original", tab_original);
        tab_principal.add("Modified", tab_modified);

        JSplitPane principal = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        principal.add(pnl_izquierdo);
        principal.add(tab_principal);
        add(principal);
        ibec.customizeUiComponent(this);
    }

    public void sendToTable(IHttpRequestResponse original, IHttpRequestResponse modified) {
        this.requestsList.add(original);
        this.modifiedRequestsList.add(modified);
        IRequestInfo requestInfo = this.helpers.analyzeRequest(original);
        this.requestsModel.addRow(new String[]{"" + contRequests++, requestInfo.getMethod()
                , requestInfo.getUrl().toString()});
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        this.requestsList.clear();
        this.modifiedRequestsList.clear();
        this.requestsModel.setRowCount(0);
    }

    public String getHost(){
        return this.hostField.getText().trim();
    }
    
}
