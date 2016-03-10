package com.elster.jupiter.http.whiteboard;

import aQute.bnd.annotation.ProviderType;

import java.net.URL;

@ProviderType
public interface Resolver {
	URL getResource(String name);
}
