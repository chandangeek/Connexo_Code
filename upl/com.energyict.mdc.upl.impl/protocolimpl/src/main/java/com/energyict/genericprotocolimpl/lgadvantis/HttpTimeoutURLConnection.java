package com.energyict.genericprotocolimpl.lgadvantis;

import com.energyict.cpo.Environment;

import java.io.IOException;
import java.io.PrintStream;
import java.net.*;

import sun.net.www.http.HttpClient;
import sun.net.www.protocol.http.HttpURLConnection;

public class HttpTimeoutURLConnection extends HttpURLConnection {

	private int httpTimeout;

	public HttpTimeoutURLConnection(URL location, int httpTimeout) 
	throws IOException {

		super(location, new HttpTimeoutHandler(httpTimeout));
		this.httpTimeout = httpTimeout;
	}

	public void connect() throws IOException {
		if (connected)
			return;

		if ("http".equals(url.getProtocol())) {
			synchronized (url) {
				http = HttpTimeoutClient.getInstance(url, httpTimeout);
			}
		} else {
			if (!(handler instanceof HttpTimeoutHandler))
				throw new IOException("Expected com.velocityreviews.HttpTimeoutConnection$HttpTimeoutHandler, got "
						+ handler.getClass());

			http = new HttpTimeoutClient(super.url, ((HttpTimeoutHandler) handler).getProxy(),
					((HttpTimeoutHandler) handler).getProxyPort(), httpTimeout);
		}

		ps = (PrintStream) http.getOutputStream();
		connected = true;
	}


	protected HttpClient getNewClient(URL url) throws IOException {
		HttpTimeoutClient rslt = new HttpTimeoutClient( url, (String)null, -1, httpTimeout );
		return rslt;
	}

	protected HttpClient getProxiedClient(URL url, String s, int i) throws IOException {
		HttpTimeoutClient rslt = new HttpTimeoutClient(url, s, i, httpTimeout);
		return rslt;
	}

}

class HttpTimeoutClient extends HttpClient {

	private final static int DEFAULTTIMEOUT = 60000;

	public HttpTimeoutClient(URL location) throws IOException {
		super(location, (String) null, -1);
	}

	public HttpTimeoutClient(URL location, String proxy, int proxyPort, int timeout) throws IOException {
		super(location, proxy, proxyPort);
	}

	public static HttpTimeoutClient getInstance(URL location, int timeout) 
	throws IOException {

		HttpTimeoutClient client = (HttpTimeoutClient) kac.get(location);


		if (client == null) {
			if (System.getProperty("proxySet") != null && System.getProperty("proxySet").equals("true"))
				client = new HttpTimeoutClient(location, System.getProperty("http.proxyHost"), Integer.parseInt(System
						.getProperty("http.proxyPort")), timeout);
			else
				client = new HttpTimeoutClient(location); // CTOR called openServer()
		} else {
			client.url = location;
		}
		return client;
	}

	protected Socket doConnect(String s, int i) throws IOException, UnknownHostException, SocketException {

		Socket socket = super.doConnect(s, i);
		socket.setSoTimeout(getTimeoutProperty());
		return socket;

	}

	protected int getTimeoutProperty(){
		String value = Environment.getDefault().getProperty("LandisHttpConnectionTimeout",null);
		if (value == null)
			return DEFAULTTIMEOUT;
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException ex) {
			return  DEFAULTTIMEOUT;
		}

	}

}
