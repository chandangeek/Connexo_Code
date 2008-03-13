package com.energyict.genericprotocolimpl.lgadvantis;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import sun.net.www.protocol.http.Handler;

class HttpTimeoutHandler extends Handler {

    private int httpTimeout = 20000;
    
    HttpTimeoutHandler(int httpTimeout) {
        this.httpTimeout = httpTimeout;
    }
    
    protected URLConnection openConnection(URL u) throws IOException {
        HttpTimeoutURLConnection c = new HttpTimeoutURLConnection(u, httpTimeout);
        return c;
    }
    
    protected String getProxy() {
        return proxy;
    }
    
    protected int getProxyPort() {
        return proxyPort;
    }
    
}