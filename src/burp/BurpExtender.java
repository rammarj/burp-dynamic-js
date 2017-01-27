/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package burp;

import burp.userinterface.Tab;
import burp.userinterface.UInterface;
import burp.util.Util;
import java.util.List;

/**
 *
 * @author Joaquin R. Martinez
 */
public class BurpExtender implements IBurpExtender, IHttpListener {

    public static IBurpExtenderCallbacks ibec;
    private UInterface uInterface;
    private IExtensionHelpers helpers;

    @Override
    public void registerExtenderCallbacks(IBurpExtenderCallbacks ibec) {
        BurpExtender.ibec = ibec;
        helpers = ibec.getHelpers();
        uInterface = new UInterface(ibec);
        ibec.registerHttpListener(this);
        ibec.addSuiteTab(new Tab("Burp Dynamic JS", uInterface));
    }

    @Override
    public void processHttpMessage(int arg0, boolean arg1, IHttpRequestResponse original) {
        if (arg1 == false && IBurpExtenderCallbacks.TOOL_PROXY == arg0 && original.getHost().equals(this.uInterface.getHost())){
            try {
                IResponseInfo originalResponse = helpers.analyzeResponse(original.getResponse());
                String mime = originalResponse.getStatedMimeType();
                String[] scriptMimes = Util.getScriptMimes();
                boolean accept = false;
                for (String scriptMime : scriptMimes) {
                    if (scriptMime.equals(mime)) {
                        accept = true;
                        break;
                    }
                }
                if (accept == false) {
                    return;
                }
                IHttpRequestResponse modified = updateRequest(original);
                IResponseInfo RespModif = helpers.analyzeResponse(modified.getResponse());

                String originalBody = helpers.bytesToString(original.getResponse())
                        .substring(originalResponse.getBodyOffset());
                String modifiedBody = helpers.bytesToString(modified.getResponse())
                        .substring(RespModif.getBodyOffset());
                
                if (!originalBody.equals(modifiedBody)) {
                    uInterface.sendToTable(original, modified);
                }
            } catch (Exception ex) { }
        }
    }

    private IHttpRequestResponse updateRequest(IHttpRequestResponse baseRequestResponse) {
        byte[] request = baseRequestResponse.getRequest();
        IRequestInfo ar = helpers.analyzeRequest(request);
        List<IParameter> parameters = ar.getParameters();
        for (IParameter get : parameters) { //remove cookies
            if (get.getType() == IParameter.PARAM_COOKIE) {
                request = helpers.removeParameter(request, get);
            }
        }
        return ibec.makeHttpRequest(baseRequestResponse.getHttpService(), request);
    }

}
