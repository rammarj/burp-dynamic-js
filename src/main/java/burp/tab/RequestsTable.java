package burp.tab;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import burp.IRequestInfo;
import burp.util.DynamicRequestResponse;

public abstract class RequestsTable extends JTable implements ListSelectionListener {

	private static final long serialVersionUID = 1L;
	private Map<String, DynamicRequestResponse> messages;
	private final DefaultTableModel requestsModel;
	private int contRequests;

	public RequestsTable() {
		this.messages = new HashMap<>();
		this.requestsModel = new DefaultTableModel(new String[] { "#id", "method", "url" }, 0);
		this.contRequests = 1;
		setModel(this.requestsModel);
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		getSelectionModel().addListSelectionListener(this);
	}

	@Override
	public boolean isCellEditable(int row, int column) {
		return false;
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		int selectedRow = getSelectedRow();
		String url = getValueAt(selectedRow, 2).toString();
		setChange(this.messages.get(url));
	}

	public abstract void setChange(DynamicRequestResponse dynamicRequestResponse);

	public void addDynamicRequestResponse(DynamicRequestResponse drr, IRequestInfo requestInfo) {
		String url = requestInfo.getUrl().toString();
		
		// not add already added values
		if (messages.containsKey(url))
			return;
		
		this.messages.put(url, drr);
		this.requestsModel.addRow(new String[] { String.valueOf(this.contRequests++), requestInfo.getMethod(), url });
	}

	public void clearTable() {
		this.requestsModel.setRowCount(0);
		this.messages.clear();
	}
	
	public boolean messageAlreadyExists(String url) {
		return this.messages.containsKey(url);
	}
}
