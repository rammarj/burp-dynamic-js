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

    private DefaultTableModel mdl_tblRequests;
    private IMessageEditor msgeditor_request, msgeditor_response, msgeditor_modified_request,
            msgeditor_modified_response;
    //private JCheckBox chb_automaticAddToList;
    private LinkedList<IHttpRequestResponse> lst_request, lst_modified_requests;
    private IExtensionHelpers helpers;
    private int contRequests;
    private JTable tbl_requests;
    private JButton btn_limpiar;
    private JTextField txt_host;

    public UInterface(IBurpExtenderCallbacks ibec) {
        //super(new BorderLayout(10,10));
        this.setBackground(Color.WHITE);
        BoxLayout boxLayout = new BoxLayout(this, BoxLayout.Y_AXIS);
        this.setLayout(boxLayout);
        this.helpers = ibec.getHelpers();
        this.lst_request = new LinkedList<>();
        this.lst_modified_requests = new LinkedList<>();
        this.btn_limpiar = new JButton("clear table");
        this.btn_limpiar.addActionListener(this);
        //this.btn_same_url_method = new JButton("Delete duplicated items")
        //this.btn_same_url_method.addActionListener(this);
        contRequests = 1;
        txt_host = new JTextField(20);
        //chb_automaticAddToList = new JCheckBox("Add request to list (If sends CSRF Tokens)");
        this.mdl_tblRequests = new DefaultTableModel(new String[]{"#id", "method", "url"}, 0);
        this.msgeditor_request = ibec.createMessageEditor(new IMessageEditorController() {
            @Override
            public IHttpService getHttpService() {
                return new IHttpServiceImpl(helpers.analyzeRequest(msgeditor_request.getMessage()).getUrl());
            }

            @Override
            public byte[] getRequest() {
                return msgeditor_request.getMessage();
            }

            @Override
            public byte[] getResponse() {
                return null;
            }
        }, false);
        this.msgeditor_response = ibec.createMessageEditor(new IMessageEditorController() {
            @Override
            public IHttpService getHttpService() {
                return new IHttpServiceImpl(helpers.analyzeRequest(msgeditor_response.getMessage()).getUrl());
            }

            @Override
            public byte[] getRequest() {
                return null;
            }

            @Override
            public byte[] getResponse() {
                return msgeditor_response.getMessage();
            }
        }, false);
        this.msgeditor_modified_request = ibec.createMessageEditor(new IMessageEditorController() {
            @Override
            public IHttpService getHttpService() {
                return new IHttpServiceImpl(helpers.analyzeRequest(msgeditor_modified_request.getMessage()).getUrl());
            }

            @Override
            public byte[] getRequest() {
                return msgeditor_modified_request.getMessage();
            }

            @Override
            public byte[] getResponse() {
                return null;
            }
        }, false);
        this.msgeditor_modified_response = ibec.createMessageEditor(new IMessageEditorController() {
            @Override
            public IHttpService getHttpService() {
                return new IHttpServiceImpl(helpers.analyzeRequest(msgeditor_modified_response.getMessage()).getUrl());
            }
            @Override
            public byte[] getRequest() {
                return null;
            }
            @Override
            public byte[] getResponse() {
                return msgeditor_modified_response.getMessage();
            }
        }, false);
        
        tbl_requests = new JTable();
        //tbl_requests.setEnabled(false);
        tbl_requests.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tbl_requests.addMouseListener(new MouseAdapter() { /*evento para cambiar de Request-Response en el Editor de mensages (MessageEditor)*/

            @Override
            public void mouseClicked(MouseEvent e) {
                int selectedRow = tbl_requests.getSelectedRow();
                if (selectedRow != -1) {
                    IHttpRequestResponse httpReqResU1 = lst_request.get(selectedRow);
                    try {
                        msgeditor_request.setMessage(httpReqResU1.getRequest(), true);
                        msgeditor_response.setMessage(httpReqResU1.getResponse(), false);
                        
                        IHttpRequestResponse httpReqResU2 = lst_modified_requests.get(selectedRow);
                        msgeditor_modified_request.setMessage(httpReqResU2.getRequest(), true);
                        msgeditor_modified_response.setMessage(httpReqResU2.getResponse(), false);

                    } catch (Exception ex) {
                    }
                }
            }
        });
        tbl_requests.setModel(this.mdl_tblRequests);
        JPanel pnl_izquierdo = new JPanel();
        BoxLayout box = new BoxLayout(pnl_izquierdo, BoxLayout.Y_AXIS);
        pnl_izquierdo.setLayout(box);
        JScrollPane scl_tblRequests = new JScrollPane();
        scl_tblRequests.setViewportView(tbl_requests);
        Border brd_pnlIdors = new TitledBorder(new LineBorder(Color.BLACK), "Suspicious List");
        scl_tblRequests.setBorder(brd_pnlIdors);
        pnl_izquierdo.add(scl_tblRequests);
        
        JPanel pnl_bottom = new JPanel();
        pnl_bottom.add(btn_limpiar);
        pnl_bottom.add(new JLabel("Host:"));
        pnl_bottom.add(txt_host);
        pnl_izquierdo.add(pnl_bottom);
        //crear tab que contiene los del usuario 1 y 2, ademas los del CSRF
        JTabbedPane tab_principal = new JTabbedPane();
        //crear panel preview HTTP
        //crear panel request preview
        JTabbedPane tab_original = new JTabbedPane();
        JTabbedPane tab_modified = new JTabbedPane();
        //agregar al tab 2 los requestst/responeses del usuario 2
        tab_original.add("Request", this.msgeditor_request.getComponent());
        tab_original.add("Response", this.msgeditor_response.getComponent());
        //agregar al tab 2 los requestst/responeses del usuario 2
        tab_modified.add("Request", this.msgeditor_modified_request.getComponent());
        tab_modified.add("Response", this.msgeditor_modified_response.getComponent());
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
        this.lst_request.add(original);
        this.lst_modified_requests.add(modified);
        IRequestInfo requestInfo = this.helpers.analyzeRequest(original);
        this.mdl_tblRequests.addRow(new String[]{"" + contRequests++, requestInfo.getMethod()
                , requestInfo.getUrl().toString()});
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        this.lst_request.clear();
        this.lst_modified_requests.clear();
        this.mdl_tblRequests.setRowCount(0);
    }

    public String getHost(){
        return this.txt_host.getText().trim();
    }
    
}
