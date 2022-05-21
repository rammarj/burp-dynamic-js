package burp.userinterface;

import burp.IBurpExtenderCallbacks;
import burp.IExtensionHelpers;
import burp.IHttpRequestResponse;
import burp.IHttpService;
import burp.IMessageEditor;
import burp.IMessageEditorController;
import burp.IRequestInfo;
import burp.util.HttpServiceImpl;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Joaquin R. Martinez
 */
public class UInterface extends JPanel implements ActionListener {

	private static final long serialVersionUID = 1L;
	
	private DefaultTableModel requestsModel;
    private IMessageEditor msgeditorRequest, msgeditorResponse, msgeditoModifiedRequest,
            msgeditorModifiedResponse;
    private LinkedList<IHttpRequestResponse> requestsList, modifiedRequestsList;
    private IExtensionHelpers helpers;
    private int contRequests;
    private JTable requestsTable;
    private JButton cleanButton;

    public UInterface(IBurpExtenderCallbacks ibec) {
        this.setBackground(Color.WHITE);
        BoxLayout boxLayout = new BoxLayout(this, BoxLayout.Y_AXIS);
        this.setLayout(boxLayout);
        this.helpers = ibec.getHelpers();
        this.requestsList = new LinkedList<>();
        this.modifiedRequestsList = new LinkedList<>();
        this.cleanButton = new JButton("clear table");
        this.cleanButton.addActionListener(this);
        contRequests = 1;
        this.requestsModel = new DefaultTableModel(new String[]{"#id", "method", "url"}, 0);
        this.msgeditorRequest = ibec.createMessageEditor(new IMessageEditorController() {
            @Override
            public IHttpService getHttpService() {
                return new HttpServiceImpl(helpers.analyzeRequest(msgeditorRequest.getMessage()).getUrl());
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
                return new HttpServiceImpl(helpers.analyzeRequest(msgeditorResponse.getMessage()).getUrl());
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
                return new HttpServiceImpl(helpers.analyzeRequest(msgeditoModifiedRequest.getMessage()).getUrl());
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
                return new HttpServiceImpl(helpers.analyzeRequest(msgeditorModifiedResponse.getMessage()).getUrl());
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
        requestsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        requestsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                int selectedRow = requestsTable.getSelectedRow();
                IHttpRequestResponse httpReqResU1 = requestsList.get(selectedRow);
                try {
                    msgeditorRequest.setMessage(httpReqResU1.getRequest(), true);
                    msgeditorResponse.setMessage(httpReqResU1.getResponse(), false);

                    IHttpRequestResponse httpReqResU2 = modifiedRequestsList.get(selectedRow);
                    msgeditoModifiedRequest.setMessage(httpReqResU2.getRequest(), true);
                    msgeditorModifiedResponse.setMessage(httpReqResU2.getResponse(), false);

                } catch (Exception ex) {
                }
            }
        });
        requestsTable.setModel(this.requestsModel);
        JPanel pnlIzquierdo = new JPanel(new BorderLayout());
        JScrollPane sclTbSuspiciuslRequests = new JScrollPane();
        sclTbSuspiciuslRequests.setViewportView(requestsTable);
        Border brdPnlSuspicius = new TitledBorder(new LineBorder(Color.BLACK), "Suspicious List");
        sclTbSuspiciuslRequests.setBorder(brdPnlSuspicius);
        pnlIzquierdo.add(sclTbSuspiciuslRequests, "Center");

        JPanel pnlClearRequests = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlClearRequests.add(cleanButton);
        pnlIzquierdo.add(pnlClearRequests, "South");
        JTabbedPane tab_principal = new JTabbedPane();
        JTabbedPane tab_original = new JTabbedPane();
        JTabbedPane tab_modified = new JTabbedPane();
        tab_original.add("Request", this.msgeditorRequest.getComponent());
        tab_original.add("Response", this.msgeditorResponse.getComponent());
        tab_modified.add("Request", this.msgeditoModifiedRequest.getComponent());
        tab_modified.add("Response", this.msgeditorModifiedResponse.getComponent());
        tab_principal.add("Original", tab_original);
        tab_principal.add("Modified", tab_modified);

        JSplitPane principal = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        principal.add(pnlIzquierdo);
        principal.add(tab_principal);
        this.add(principal);
        ibec.customizeUiComponent(this);
    }

    public void sendToTable(IHttpRequestResponse original, IHttpRequestResponse modified) {
        this.requestsList.add(original);
        this.modifiedRequestsList.add(modified);
        IRequestInfo requestInfo = this.helpers.analyzeRequest(original);
        this.requestsModel.addRow(new String[]{"" + contRequests++, requestInfo.getMethod(),
             requestInfo.getUrl().toString()});
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        this.requestsList.clear();
        this.modifiedRequestsList.clear();
        this.requestsModel.setRowCount(0);
    }
    
}
