/**
 * 
 */
package com.elster.jupiter.http.whiteboard;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author kha
 *
 */
public final class FileResolver implements Resolver {

	@Override
	public URL getResource(String name) {
		try {			
			return new URL("file://" + name );
		} catch (MalformedURLException ex) {
			ex.printStackTrace();
			throw new RuntimeException(ex);
		}	
	}

}
