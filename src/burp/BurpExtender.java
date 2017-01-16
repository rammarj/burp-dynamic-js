/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package burp;

import burp.userinterface.Tab;
import burp.userinterface.UInterface;
import burp.util.Util;
import java.io.IOException;
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
        BurpExtender.ibec = ibec;//guardar
        helpers = ibec.getHelpers();
        uInterface = new UInterface(ibec);
        ibec.registerHttpListener(this);
        /*agregar el nuevo tab a burp*/
        //instanciar la interfaz
        ibec.addSuiteTab(new Tab("Burp Dynamic JS", uInterface));
    }

    @Override
    public void processHttpMessage(int arg0, boolean arg1, IHttpRequestResponse original) {
        if (arg1 == false && IBurpExtenderCallbacks.TOOL_PROXY == arg0 && original.getHost().equals(this.uInterface.getHost())){
            try {
                IResponseInfo RespOrig = helpers.analyzeResponse(original.getResponse());
                String mime = RespOrig.getStatedMimeType();
                String[] scriptMimes = Util.getScriptMimes();
                boolean aceptar = false;
                for (String scriptMime : scriptMimes) {
                    if (scriptMime.equals(mime)) {
                        aceptar = true;
                        break;
                    }
                }
                if (aceptar == false) {
                    return;
                }
                IHttpRequestResponse modified = actualizarRequest(original);
                IResponseInfo RespModif = helpers.analyzeResponse(modified.getResponse());

                String bodyOrig = helpers.bytesToString(original.getResponse()).substring(RespOrig.getBodyOffset());
                String bodyModif = helpers.bytesToString(modified.getResponse()).substring(RespModif.getBodyOffset());
                if (!bodyOrig.equals(bodyModif)) {
                    uInterface.sendToTable(original, modified);
                }
            } catch (Exception ex) {
                try {
                    ibec.getStderr().write(ex.getMessage().getBytes());
                } catch (IOException ex1) {
                }
            }
        }
    }

    private IHttpRequestResponse actualizarRequest(IHttpRequestResponse baseRequestResponse) {
        byte[] request = baseRequestResponse.getRequest();
        IRequestInfo ar = helpers.analyzeRequest(request);
        //String cookieHeader = getCookieHeader(modified.getHeaders());
        List<IParameter> parameters = ar.getParameters();
        for (IParameter get : parameters) { //remover los parametros cookie
            if (get.getType() == IParameter.PARAM_COOKIE) {
                request = helpers.removeParameter(request, get);
            }
        }
        return ibec.makeHttpRequest(baseRequestResponse.getHttpService(), request);
    }

}
