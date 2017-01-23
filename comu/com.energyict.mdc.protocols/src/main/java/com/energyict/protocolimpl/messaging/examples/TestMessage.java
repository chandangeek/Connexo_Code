package com.energyict.protocolimpl.messaging.examples;

import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.messaging.AnnotatedMessage;
import com.energyict.protocolimpl.messaging.RtuMessageAttribute;
import com.energyict.protocolimpl.messaging.RtuMessageDescription;

import java.math.BigDecimal;
import java.util.Date;
import java.util.TimeZone;

/**
 * Copyrights EnergyICT
 * Date: 6/6/12
 * Time: 11:16 PM
 */
@RtuMessageDescription(category = "Test messages", tag = "TestMessage", description = "Test message 1", advanced = true)
public interface TestMessage extends AnnotatedMessage {

    @RtuMessageAttribute(tag = "SerialNumber", defaultValue = "123456789")
    int getSerialNumber();

    @RtuMessageAttribute(tag = "UserName", required = true)
    String getUserName();

    @RtuMessageAttribute(tag = "Password", required = true)
    String getPassword();

    @RtuMessageAttribute(tag = "ObisCode", required = true)
    ObisCode getObisCode();

    @RtuMessageAttribute(tag = "TestDate", required = true)
    Date getTestDate();

    @RtuMessageAttribute(tag = "TrueOrFalse", required = true)
    boolean isTrueOrFalse();

    @RtuMessageAttribute(tag = "TimeZone", required = true)
    TimeZone getTimeZone();

    @RtuMessageAttribute(tag = "Unit", required = true)
    Unit getUnit();

    @RtuMessageAttribute(tag = "BigDecimal", required = true)
    BigDecimal getBigDecimal();

}
