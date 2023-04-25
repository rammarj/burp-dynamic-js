package burp;

import burp.tab.Tab;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Joaquin R. Martinez (@rammarj)
 */
public class BurpExtender implements IBurpExtender, IHttpListener {

	public static IBurpExtenderCallbacks ibec;
	private Tab burpTab;
	private IExtensionHelpers helpers;

	@Override
	public void registerExtenderCallbacks(IBurpExtenderCallbacks ibec) {
		BurpExtender.ibec = ibec;
		helpers = ibec.getHelpers();
		burpTab = new Tab(ibec);
		ibec.registerHttpListener(this);
		ibec.addSuiteTab(burpTab);
	}

	@Override
	public void processHttpMessage(int toolFlag, boolean isRequest, IHttpRequestResponse original) {
		// interested only in responses.
		if (isRequest)
			return;

		// only process in proxy and spider tools.
		if (IBurpExtenderCallbacks.TOOL_PROXY != toolFlag && IBurpExtenderCallbacks.TOOL_SPIDER != toolFlag)
			return;

		IRequestInfo requestInfo = helpers.analyzeRequest(original);
		if (burpTab.isOnlyInScopeDomains() && !ibec.isInScope(requestInfo.getUrl()))
			return;
		
		if (burpTab.messageAlreadyExists(requestInfo.getUrl().toString()))
			return;
		
		IResponseInfo originalResponse = helpers.analyzeResponse(original.getResponse());
		List<String> mimeTypes = Arrays.asList("script", "JSON", "CSS");
		if (!mimeTypes.stream().anyMatch(e -> e.equals(originalResponse.getStatedMimeType())))
			return;
		
		
		IHttpRequestResponse secondRequest = makeRequestWithoutCookies(original);
		IResponseInfo secondResponse = helpers.analyzeResponse(secondRequest.getResponse());
		String originalBody = helpers.bytesToString(original.getResponse())
				.substring(originalResponse.getBodyOffset());
		String modifiedBody = helpers.bytesToString(secondRequest.getResponse()).substring(secondResponse.getBodyOffset());

		if (!originalBody.equals(modifiedBody))
			burpTab.sendToTable(original, secondRequest);
		
	}

	private IHttpRequestResponse makeRequestWithoutCookies(IHttpRequestResponse baseRequestResponse) {
		byte[] request = baseRequestResponse.getRequest();
		IRequestInfo ri = helpers.analyzeRequest(request);
		List<IParameter> parameters = ri.getParameters();
		for (IParameter p : parameters) {
			if (p.getType() == IParameter.PARAM_COOKIE) {
				request = helpers.removeParameter(request, p);
			}
		}
		return ibec.makeHttpRequest(baseRequestResponse.getHttpService(), request);
	}

}
