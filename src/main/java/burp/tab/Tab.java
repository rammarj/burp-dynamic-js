package burp.tab;

import burp.IBurpExtenderCallbacks;
import burp.IExtensionHelpers;
import burp.IHttpRequestResponse;
import burp.IHttpService;
import burp.IMessageEditor;
import burp.IMessageEditorController;
import burp.IRequestInfo;
import burp.ITab;
import burp.util.DynamicRequestResponse;
import burp.util.HttpServiceImpl;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

/**
 *
 * @author Joaquin R. Martinez
 */
public class Tab extends JPanel implements ITab {

	private static final long serialVersionUID = 1L;

	private IMessageEditor msgeditorRequest, msgeditorResponse, msgeditoModifiedRequest, msgeditorModifiedResponse;
	private final IExtensionHelpers helpers;
	private final RequestsTable requestsTable;
	private boolean onlyInScopeDomains;

	public Tab(IBurpExtenderCallbacks ibec) {
		this.setBackground(Color.WHITE);
		BoxLayout boxLayout = new BoxLayout(this, BoxLayout.Y_AXIS);
		this.setLayout(boxLayout);
		this.helpers = ibec.getHelpers();
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
		requestsTable = createRequestsTable(this.msgeditorRequest, this.msgeditorResponse, this.msgeditoModifiedRequest,
				this.msgeditorModifiedResponse);

		JTabbedPane rightSideMessagesTab = new JTabbedPane();
		JTabbedPane originalMessageTab = new JTabbedPane();
		JTabbedPane modifiedMessageTab = new JTabbedPane();
		originalMessageTab.add("Request", this.msgeditorRequest.getComponent());
		originalMessageTab.add("Response", this.msgeditorResponse.getComponent());
		modifiedMessageTab.add("Request", this.msgeditoModifiedRequest.getComponent());
		modifiedMessageTab.add("Response", this.msgeditorModifiedResponse.getComponent());
		rightSideMessagesTab.add("Original", originalMessageTab);
		rightSideMessagesTab.add("Modified", modifiedMessageTab);

		JSplitPane mainContainer = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		mainContainer.add(createLeftSidePanel());
		mainContainer.add(rightSideMessagesTab);
		this.add(mainContainer);
		ibec.customizeUiComponent(this);
	}

	private RequestsTable createRequestsTable(IMessageEditor msgeditorRequest, IMessageEditor msgeditorResponse,
			IMessageEditor msgeditoModifiedRequest, IMessageEditor msgeditorModifiedResponse) {
		return new RequestsTable() {
			private static final long serialVersionUID = 1L;

			@Override
			public void setChange(DynamicRequestResponse dynamicRequestResponse) {
				IHttpRequestResponse original = dynamicRequestResponse.getOriginal();
				msgeditorRequest.setMessage(original.getRequest(), true);
				msgeditorResponse.setMessage(original.getResponse(), false);

				IHttpRequestResponse modified = dynamicRequestResponse.getModified();
				msgeditoModifiedRequest.setMessage(modified.getRequest(), true);
				msgeditorModifiedResponse.setMessage(modified.getResponse(), false);
			}

			@Override
			public void clearTable() {
				super.clearTable();
				msgeditorRequest.setMessage(null, false);
				msgeditorResponse.setMessage(null, false);
				msgeditoModifiedRequest.setMessage(null, false);
				msgeditorModifiedResponse.setMessage(null, false);

			}
		};
	}

	private JPanel createLeftSidePanel() {
		JPanel panel = new JPanel(new BorderLayout());
		JCheckBox isInScopeCheckBox = new JCheckBox("Validate only in scope domains");
		isInScopeCheckBox.addChangeListener(e -> {
			this.onlyInScopeDomains = isInScopeCheckBox.isSelected();
		});
		panel.add(isInScopeCheckBox, BorderLayout.NORTH);
		panel.add(createRequestTableScroll(), BorderLayout.CENTER);
		panel.add(createClearRequestsButtonPanel(), BorderLayout.SOUTH);
		return panel;
	}

	private JScrollPane createRequestTableScroll() {
		JScrollPane sclTbSuspiciuslRequests = new JScrollPane();
		sclTbSuspiciuslRequests.setViewportView(requestsTable);
		Border brdPnlSuspicius = new TitledBorder(new LineBorder(Color.BLACK), "Suspicious List");
		sclTbSuspiciuslRequests.setBorder(brdPnlSuspicius);
		return sclTbSuspiciuslRequests;
	}

	private JPanel createClearRequestsButtonPanel() {
		JPanel pnlClearRequests = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JButton cleanButton = new JButton("Clear table");
		cleanButton.addActionListener(e -> this.requestsTable.clearTable());
		pnlClearRequests.add(cleanButton);
		return pnlClearRequests;
	}

	public void sendToTable(IHttpRequestResponse original, IHttpRequestResponse modified) {
		IRequestInfo requestInfo = this.helpers.analyzeRequest(original);
		this.requestsTable.addDynamicRequestResponse(new DynamicRequestResponse(original, modified), requestInfo);
	}

	public boolean isOnlyInScopeDomains() {
		return onlyInScopeDomains;
	}

	@Override
	public String getTabCaption() {
		return "Burp Dynamic JS";
	}

	@Override
	public Component getUiComponent() {
		return this;
	}

	public boolean messageAlreadyExists(String url) {
		return this.requestsTable.messageAlreadyExists(url);
	}
}
