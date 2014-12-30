package com.energyict.protocols.mdc.services.impl;

import com.energyict.dialer.core.impl.StreamConnectionImpl;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import java.util.Map;

/**
 * Provides an implementation for the {@link EnvironmentPropertyService} interface
 * that uses the OSGi framework of setting/getting properties.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-12-30 (08:32)
 */
@Component(name="com.energyict.mdc.service.deviceprotocols.properties", service = EnvironmentPropertyService.class, immediate = true)
@SuppressWarnings("unused")
public class EnvironmentPropertyServiceImpl implements EnvironmentPropertyService {

    private volatile long openSerialAndFlush;
    private volatile long setParityAndFlush;
    private volatile long setParamsAndFlush;
    private volatile long setBaudrateAndFlush;
    private volatile boolean rs485SoftwareDriven;
    private volatile String dcdComPortsToIgnore;
    private volatile int datagramInputStreamBufferSize;

    @Activate
    public void activate(Map<String, Object> props) {
        if (props != null) {
            openSerialAndFlush = toLong(props.get(StreamConnectionImpl.OPEN_SERIAL_AND_FLUSH_ENVIRONMENT_PROPERTY_NAME));
            setParityAndFlush = toLong(props.get(StreamConnectionImpl.SET_PARITY_AND_FLUSH_ENVIRONMENT_PROPERTY_NAME));
            setParamsAndFlush = toLong(props.get(StreamConnectionImpl.SET_PARAMS_AND_FLUSH_ENVIRONMENT_PROPERTY_NAME));
            setBaudrateAndFlush = toLong(props.get(StreamConnectionImpl.SET_BAUDRATE_AND_FLUSH_ENVIRONMENT_PROPERTY_NAME));
            rs485SoftwareDriven = toBoolean(props.get(StreamConnectionImpl.RS485_SOFTWARE_DRIVEN_ENVIRONMENT_PROPERTY_NAME));
            dcdComPortsToIgnore = toString(props.get(StreamConnectionImpl.IGNORE_DCD_COM_PORTS_ENVIRONMENT_PROPERTY_NAME));
            datagramInputStreamBufferSize = toInt(props.get(StreamConnectionImpl.DATAGRAM_INPUTSTREAM_BUFFER_ENVIRONMENT_PROPERTY_NAME));
        }
    }

    @Override
    public long getOpenSerialAndFlush() {
        return openSerialAndFlush;
    }

    @Override
    public long getSetParityAndFlush() {
        return setParityAndFlush;
    }

    @Override
    public long getSetParamsAndFlush() {
        return setParamsAndFlush;
    }

    @Override
    public long getSetBaudrateAndFlush() {
        return setBaudrateAndFlush;
    }

    @Override
    public boolean isRs485SoftwareDriven() {
        return rs485SoftwareDriven;
    }

    @Override
    public String getDcdComPortsToIgnore() {
        return dcdComPortsToIgnore;
    }

    @Override
    public int getDatagramInputStreamBufferSize() {
        return datagramInputStreamBufferSize;
    }

    private int toInt(Object property) {
        Integer integer = (Integer) property;
        return integer.intValue();
    }

    private long toLong(Object property) {
        Long value = (Long) property;
        return value.longValue();
    }

    private boolean toBoolean(Object property) {
        return Boolean.TRUE.equals(property);
    }

    private String toString(Object property) {
        return (String) property;
    }

}