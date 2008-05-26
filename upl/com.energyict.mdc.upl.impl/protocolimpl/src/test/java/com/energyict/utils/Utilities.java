package com.energyict.utils;

import java.io.IOException;
import java.util.Properties;

import com.energyict.cbo.ApplicationException;
import com.energyict.cbo.Utils;
import com.energyict.cpo.Environment;

public class Utilities {
	public static void createEnvironment() {
    	try {
    		Properties properties = new Properties();
    		properties.load(Utils.class.getResourceAsStream( "/eiserver.properties" ));
			Environment.setDefault(properties);
		} catch (IOException e) {
            throw new ApplicationException(e);
		}
    }
}
