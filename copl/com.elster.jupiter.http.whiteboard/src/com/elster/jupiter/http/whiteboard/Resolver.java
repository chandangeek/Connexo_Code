package com.elster.jupiter.http.whiteboard;

import java.net.URL;

public interface Resolver {
	URL getResource(String name);
}
