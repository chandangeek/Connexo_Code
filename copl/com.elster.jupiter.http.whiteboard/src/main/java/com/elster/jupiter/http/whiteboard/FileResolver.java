/**
 * 
 */
package com.elster.jupiter.http.whiteboard;

import com.elster.jupiter.orm.UnderlyingSQLFailedException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author kha
 *
 */
public final class FileResolver implements Resolver {

    private static final Logger LOGGER = Logger.getLogger(FileResolver.class.getName());

	@Override
	public URL getResource(String name) {
		try {			
			return new URL("file:///" + name );
		} catch (MalformedURLException ex) {
			LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
			throw new UnderlyingNetworkException(ex);
		}	
	}

}
