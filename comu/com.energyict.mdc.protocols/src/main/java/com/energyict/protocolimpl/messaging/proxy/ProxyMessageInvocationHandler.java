/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.messaging.proxy;

import com.energyict.mdc.protocol.api.device.data.MessageEntry;

import com.energyict.protocolimpl.messaging.AnnotatedMessage;
import com.energyict.protocolimpl.messaging.AnnotatedMessaging;
import com.energyict.protocolimpl.messaging.RtuMessageAttribute;
import com.energyict.protocolimpl.messaging.RtuMessageDescription;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProxyMessageInvocationHandler implements InvocationHandler {

    private final Map<String, String> attributes;
    private final Class<? extends AnnotatedMessage> message;
    private final MessageEntry messageEntry;
    private final RtuMessageDescription rtuMessageDescription;

    public ProxyMessageInvocationHandler(final HashMap<String, String> attributes, final Class<? extends AnnotatedMessage> message, final MessageEntry messageEntry, final String tagName) throws IOException {
        if (message == null) {
            throw new IllegalArgumentException("AnnotatedMessage is required for ProxyMessageInvocationHandler but was [null]!");
        }

        final List<RtuMessageDescription> descriptions = AnnotatedMessaging.getDescriptionsForMessageClass(message);

        if (descriptions == null || descriptions.size() == 0) {
        	throw new IllegalArgumentException("RtuMessageDescription(s) annotation missing for message [" + message.getName() + "]!");
        }

        final Method[] methods = message.getMethods();
        for (final Method method : methods) {
            if (method.isAnnotationPresent(RtuMessageAttribute.class)) {
                final RtuMessageAttribute messageAttribute = method.getAnnotation(RtuMessageAttribute.class);
                if (messageAttribute.required() && !attributes.containsKey(messageAttribute.tag())) {
                    throw new IOException("Attribute [" + messageAttribute.tag() + "] is required but seems to be missing!");
                }
            }
        }

        this.rtuMessageDescription = AnnotatedMessaging.getDescriptionThatMatchesTagName(tagName, message);
        this.attributes = attributes;
        this.message = message;
        this.messageEntry = messageEntry;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        final Method getMessageEntryMethod = AnnotatedMessage.class.getMethod("getMessageEntry");
        final Method getRtuMessageAttribute = AnnotatedMessage.class.getMethod("getRtuMessageDescription");

        if (method.isAnnotationPresent(RtuMessageAttribute.class)) {
            final String attributeStringValue = getAttributeStringValue(method);
            final Class<?> returnType = method.getReturnType();
            return ProxyUtils.toCorrectReturnType(attributeStringValue, returnType);
        }

        if (isObjectMethod(method)) {
            return method.invoke(this, args);
        }

        if (method.equals(getMessageEntryMethod)) {
            return this.messageEntry;
        }

        if (method.equals(getRtuMessageAttribute)) {
            return this.rtuMessageDescription;
        }

        return null;
    }

    /**
     * Get the string value from a method, or the default value if the attribute was missing but not required.
     *
     * @param method The method to get the value from
     * @return The value of the attribute method
     */
    private final String getAttributeStringValue(final Method method) {
        if (!method.isAnnotationPresent(RtuMessageAttribute.class)) {
            throw new IllegalArgumentException("Method [" + method + "] Has no RtuMessageAttribute annotation! This should never happen at this point in code.");
        }
        final RtuMessageAttribute rtuMessageAttribute = method.getAnnotation(RtuMessageAttribute.class);
        final String value = attributes.get(rtuMessageAttribute.tag());
        if (value == null) {
            if (rtuMessageAttribute.required()) {
                throw new IllegalStateException("Attribute [" + rtuMessageAttribute.tag() + "] is required but was 'null'! This should never happen at this point in code.");
            }
            return rtuMessageAttribute.defaultValue();
        }
        return value;
    }

    private boolean isObjectMethod(Method method) {
        Method[] methods = Object.class.getMethods();
        for (Method objectMethod : methods) {
            if (objectMethod.equals(method)) {
                return true;
            }
        }
        return false;
    }

    public final String toString() {
        return "Proxy implementation class for [" + message.getName() + "]";
    }
}
