/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocols.impl.channels.serial.optical.dlms;

import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.energyict.mdc.protocol.api.ConnectionProvider;
import com.energyict.protocols.impl.channels.serial.SioSerialConnectionProperties;

import java.math.BigDecimal;

/**
 * Provides an implementation for the {@link PersistentDomainExtension} interface
 * for all the properties of the {@link LegacyOpticalDlmsConnectionType}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-04 (13:12)
 */
public class LegacyOpticalDlmsConnectionProperties extends SioSerialConnectionProperties {

    public static final BigDecimal DEFAULT_ADDRESSING_MODE = BigDecimal.valueOf(2);
    public static final BigDecimal DEFAULT_SERVER_MAC_ADDRESS = BigDecimal.ONE;
    public static final BigDecimal DEFAULT_SERVER_UPPER_MAC_ADDRESS = new BigDecimal(17);
    public static final BigDecimal DEFAULT_SERVER_LOWER_MAC_ADDRESS = BigDecimal.ONE;
    public static final BigDecimal DEFAULT_DATA_LINK_LAYER_TYPE = BigDecimal.ONE;

    public enum Field {
        ADDRESSING_MODE("addressingMode", "ADDRESSINGMODE"),
        SERVER_MAC_ADDRESS("serverMacAddress", "SERVERMACADDRESS"),
        SERVER_LOWER_MAC_ADDRESS("serverLowerMacAddress", "SERVERLOWERMACADDRESS"),
        SERVER_UPPER_MAC_ADDRESS("serverUpperMacAddress", "SERVERUPPERMACADDRESS"),
        DATA_LINK_LAYER_TYPE("dataLinkLayerType", "DATALINKLAYERTYPE");

        Field(String javaName, String databaseName) {
            this.javaName = javaName;
            this.databaseName = databaseName;
        }

        private final String javaName;
        private final String databaseName;

        public String javaName() {
            return javaName;
        }

        public String databaseName() {
            return databaseName;
        }
    }

    private BigDecimal addressingMode;
    private BigDecimal serverMacAddress;
    private BigDecimal serverLowerMacAddress;
    private BigDecimal serverUpperMacAddress;
    private BigDecimal dataLinkLayerType;

    @Override
    public void copyFrom(ConnectionProvider connectionProvider, CustomPropertySetValues propertyValues, Object... additionalPrimaryKeyValues) {
        super.copyFrom(connectionProvider, propertyValues);
        this.addressingMode = (BigDecimal) propertyValues.getProperty(Field.ADDRESSING_MODE.javaName());
        this.serverMacAddress = (BigDecimal) propertyValues.getProperty(Field.SERVER_MAC_ADDRESS.javaName());
        this.serverLowerMacAddress = (BigDecimal) propertyValues.getProperty(Field.SERVER_LOWER_MAC_ADDRESS.javaName());
        this.serverUpperMacAddress = (BigDecimal) propertyValues.getProperty(Field.SERVER_UPPER_MAC_ADDRESS.javaName());
        this.dataLinkLayerType = (BigDecimal) propertyValues.getProperty(Field.DATA_LINK_LAYER_TYPE.javaName());
    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        super.copyTo(propertySetValues);
        this.copyTo(propertySetValues, Field.ADDRESSING_MODE.javaName(), this.addressingMode);
        this.copyTo(propertySetValues, Field.SERVER_MAC_ADDRESS.javaName(), this.serverMacAddress);
        this.copyTo(propertySetValues, Field.SERVER_LOWER_MAC_ADDRESS.javaName(), this.serverLowerMacAddress);
        this.copyTo(propertySetValues, Field.SERVER_UPPER_MAC_ADDRESS.javaName(), this.serverUpperMacAddress);
        this.copyTo(propertySetValues, Field.DATA_LINK_LAYER_TYPE.javaName(), this.dataLinkLayerType);
    }

    private void copyTo(CustomPropertySetValues propertySetValues, String propertyName, Object propertyValue) {
        if (propertyValue != null) {
            propertySetValues.setProperty(propertyName, propertyValue);
        }
    }

}