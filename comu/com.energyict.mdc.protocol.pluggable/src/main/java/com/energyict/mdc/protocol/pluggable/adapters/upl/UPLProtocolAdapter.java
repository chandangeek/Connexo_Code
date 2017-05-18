package com.energyict.mdc.protocol.pluggable.adapters.upl;

/**
 * @author khe
 * @since 13/02/2017 - 14:42
 */
public interface UPLProtocolAdapter<T> {

    /**
     * Return the class of the protocol that is being adapted
     */
    Class getActualClass();

    /**
     * Return the actual UPL protocol (DeviceProtocol, SmartMeterProtocol or MeterProtocol) that is being adapted
     */
    T getActual();

}