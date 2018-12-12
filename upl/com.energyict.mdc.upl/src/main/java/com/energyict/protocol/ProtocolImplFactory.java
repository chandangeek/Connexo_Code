/*
 * ProtocolImplFactory.java
 *
 * Created on 2 juni 2005, 10:53
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocol;

import com.energyict.protocol.meteridentification.IdentificationFactory;

import java.io.IOException;

/**
 * @author Koen
 *         Factory class that returns interfaces to protocolimple package specific implementations.
 *         Ths class hides all protocolimpl dependencies in other packages!
 */
public class ProtocolImplFactory {

    /**
     * Creates a new instance of ProtocolImplFactory
     */
    private ProtocolImplFactory() {
    }

    static private ProtocolImplFactory getInstance() {
        return new ProtocolImplFactory();
    }

    static public ProtocolInstantiator getProtocolInstantiator(String className) throws IOException {
        ProtocolInstantiator pi = (ProtocolInstantiator) getInstance().getInstance("com.energyict.protocolimpl.base.ProtocolInstantiatorImpl");
        pi.buildInstance(className);
        return pi;
    }

    static public IdentificationFactory getIdentificationFactory() throws IOException {
        IdentificationFactory ifac = (IdentificationFactory) getInstance().getInstance("com.energyict.protocolimpl.meteridentification.IdentificationFactoryImpl");
        return ifac;
    }


    static public String getProtocolImplVersion() {
        try {
            Class cls = Class.forName("com.energyict.protocolimpl.base.ProtocolVersionImpl");
            return cls.getPackage().getSpecificationVersion();
        } catch (ClassNotFoundException ex) {
            return null;
        }
    }

    private Object getInstance(String className) throws IOException {
        try {
            return (Class.forName(className).newInstance());
        } catch (ClassNotFoundException e) {
            throw new IOException("instantiateProtocol(), ClassNotFoundException, " + e.getMessage());
        } catch (InstantiationException e) {
            throw new IOException("instantiateProtocol(), InstantiationException, " + e.getMessage());
        } catch (IllegalAccessException e) {
            throw new IOException("instantiateProtocol(), IllegalAccessException, " + e.getMessage());
        } catch (Exception e) {
            throw new IOException("instantiateProtocol(), Exception, " + e.getMessage());
        }

    } // private void instantiateProtocol(String className)

}
