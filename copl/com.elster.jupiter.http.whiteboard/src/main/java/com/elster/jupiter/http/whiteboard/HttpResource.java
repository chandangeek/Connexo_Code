package com.elster.jupiter.http.whiteboard;

final public class HttpResource {
	private final String alias;
	private final String localName;
	private final Resolver resolver;
	
	public HttpResource(String alias, String localName, Resolver resolver) {
		this.alias = alias;
		this.localName = localName;
		this.resolver = resolver;	
	}

	public String getAlias() {
		return alias;
	}

	public String getLocalName() {
		return localName;
	}

	public Resolver getResolver() {
		return resolver;
	}
	
}
