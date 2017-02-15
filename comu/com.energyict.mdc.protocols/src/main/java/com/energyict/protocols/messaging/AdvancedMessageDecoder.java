/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocols.messaging;

import com.energyict.mdc.common.ApplicationException;
import com.energyict.mdc.protocol.api.legacy.dynamic.ConfigurationSupport;
import com.energyict.mdc.protocol.api.messaging.AdvancedMessaging;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


/**
 * AdvancedMessageDecoder is resonsible for returning the specific messageBuilder for a specific rtuMessageShadow
 *
 * @Author pdo
 */
public class AdvancedMessageDecoder {

    private ConfigurationSupport protocol;

    public AdvancedMessageDecoder(ConfigurationSupport protocol) {
        this.protocol = protocol;
    }

    private boolean isAdvancedMessaging(Class<?> intfase) {
        return (AdvancedMessaging.class.isAssignableFrom(intfase));
    }

    private MessageBuilder getMessageBuilder(Class<?> advancedMessaging) {
        Method methods[] = advancedMessaging.getMethods();
        for (int i = 0; i < methods.length; i++) {
            Method m = methods[i];
            if (MessageBuilder.class.isAssignableFrom(m.getReturnType())) {
                Object params[] = new Object[0];
                try {
                    return (MessageBuilder) m.invoke(protocol, params);
                } catch (InvocationTargetException e) {
                    throw new ApplicationException(e);
                } catch (IllegalAccessException e) {
                    throw new ApplicationException(e);
                }
            }
        }
        return null;
    }

    public MessageBuilder getMessageBuilder(String xml) {
        Class<?> interfaces[] = protocol.getClass().getInterfaces();
        for (int i = 0; i < interfaces.length; i++) {
            if (isAdvancedMessaging(interfaces[i])) {
                try {
                    MessageBuilder builder = getMessageBuilder(interfaces[i]);
                    builder.initFromXml(xml);
                    return builder;
                } catch (SAXException e) {
                    // try next
                } catch (IOException e) {
                    throw new ApplicationException(e);
                    // should not happen
                }
            }
        }
        return null;
    }
}

