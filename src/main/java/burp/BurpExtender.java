package burp;

import burp.userinterface.Tab;
import burp.userinterface.UInterface;
import burp.util.Util;
import java.util.List;
import javax.swing.JOptionPane;

/**
 *
 * @author Joaquin R. Martinez (@rammarj)
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
        if (arg1 == false && 
                (IBurpExtenderCallbacks.TOOL_PROXY == arg0 || IBurpExtenderCallbacks.TOOL_SPIDER == arg0) 
                && ibec.isInScope( helpers.analyzeRequest(original.getResponse()).getUrl())){
            try {
                IResponseInfo originalResponse = helpers.analyzeResponse(original.getResponse());
                if (! Util.getScriptMimes().stream().anyMatch(e -> e.equals(originalResponse.getStatedMimeType()))) {
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
            } catch (Exception ex) { 
            	JOptionPane.showMessageDialog(uInterface, ex.toString(), "Error", JOptionPane.ERROR_MESSAGE);            	
            }
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
