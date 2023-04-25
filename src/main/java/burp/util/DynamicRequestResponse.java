package burp.util;

import burp.IHttpRequestResponse;

public class DynamicRequestResponse {
	
	private IHttpRequestResponse original; 
	private IHttpRequestResponse modified;

	public DynamicRequestResponse(IHttpRequestResponse original, IHttpRequestResponse modified) {
		this.original = original;
		this.modified = modified;
	}
	
	public IHttpRequestResponse getModified() {
		return modified;
	}
	
	public IHttpRequestResponse getOriginal() {
		return original;
	}
}
