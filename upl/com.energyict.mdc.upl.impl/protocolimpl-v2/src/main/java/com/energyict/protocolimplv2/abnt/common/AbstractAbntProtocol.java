package com.energyict.protocolimplv2.abnt.common;

import com.energyict.mdc.protocol.DeviceProtocol;

import java.util.TimeZone;

/**
 * @author sva
 * @since 28/08/2014 - 9:36
 */
public abstract class AbstractAbntProtocol implements DeviceProtocol {

    public abstract RequestFactory getRequestFactory();

    public abstract AbntProperties getProperties();

    public abstract TimeZone getTimeZone();

}