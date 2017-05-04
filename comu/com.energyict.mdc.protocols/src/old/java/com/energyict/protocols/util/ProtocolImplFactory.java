/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * ProtocolImplFactory.java
 *
 * Created on 2 juni 2005, 10:53
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocols.util;

import com.energyict.mdc.protocol.api.inbound.IdentificationFactory;

import java.io.IOException;

/**
 * @author Koen
 *         Factory class that returns interfaces to protocolimpl package specific implementations.
 *         Ths class hides all protocolimpl dependencies in other packages!
 */
public class ProtocolImplFactory {

    private static ProtocolImplFactory getInstance() {
        return new ProtocolImplFactory();
    }

    public static ProtocolInstantiator getProtocolInstantiator(String className) throws IOException {
        ProtocolInstantiator pi = (ProtocolInstantiator) getInstance().getInstance("com.energyict.protocolimpl.base.ProtocolInstantiatorImpl");
        pi.buildInstance(className);
        return pi;
    }

    public static IdentificationFactory getIdentificationFactory() throws IOException {
        return (IdentificationFactory) getInstance().getInstance("com.energyict.protocolimpl.meteridentification.IdentificationFactoryImpl");
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

    }

    private ProtocolImplFactory() {}

}