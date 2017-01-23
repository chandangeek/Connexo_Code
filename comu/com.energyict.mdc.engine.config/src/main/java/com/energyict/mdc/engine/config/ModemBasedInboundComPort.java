package com.energyict.mdc.engine.config;

import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.channels.serial.SerialPortConfiguration;

import java.math.BigDecimal;
import java.util.List;

/**
 * Models an {@link InboundComPort} that is connected to a modem.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-03-27 (17:05)
 */
public interface ModemBasedInboundComPort extends InboundComPort {

    /**
     * Gets the number of rings that this ComPort will wait
     * before picking up the receiver when an incoming
     * call is received.
     *
     * @return The number of rings to wait before pickup up the receiver
     */
    int getRingCount();
    void setRingCount(int ringCount);

    int getMaximumDialErrors();
    void setMaximumDialErrors(int max);

    TimeDuration getConnectTimeout();
    void setConnectTimeout(TimeDuration connectTimeout);

    TimeDuration getDelayAfterConnect();
    void setDelayAfterConnect(TimeDuration connectTimeout);

    TimeDuration getDelayBeforeSend();
    void setDelayBeforeSend(TimeDuration connectTimeout);

    TimeDuration getAtCommandTimeout();
    void setAtCommandTimeout(TimeDuration connectTimeout);

    BigDecimal getAtCommandTry();
    void setAtCommandTry(BigDecimal atCommandTry);

    List<String> getModemInitStrings();
    void setModemInitStrings(List<String> initStrings);

    List<String> getGlobalModemInitStrings();
    void setGlobalModemInitStrings(List<String> globalModemInitStrings);

    String getAddressSelector();
    void setAddressSelector(String addressSelector);

    String getPostDialCommands();
    void setPostDialCommands(String postDialCommands);

    SerialPortConfiguration getSerialPortConfiguration();
    void setSerialPortConfiguration(SerialPortConfiguration serialPortConfiguration);

    interface ModemBasedInboundComPortBuilder extends InboundComPortBuilder<ModemBasedInboundComPortBuilder, ModemBasedInboundComPort>{
        ModemBasedInboundComPortBuilder delayAfterConnect(TimeDuration delayAfterConnect);
        ModemBasedInboundComPortBuilder delayBeforeSend(TimeDuration delayBeforeSend);
        ModemBasedInboundComPortBuilder atCommandTry(BigDecimal atCommandTry);
        ModemBasedInboundComPortBuilder atModemInitStrings(List<String> initStrings);
        ModemBasedInboundComPortBuilder globalAtModemInitStrings(List<String> globalAtModemInitStrings);
        ModemBasedInboundComPortBuilder addressSelector(String addressSelector);
        ModemBasedInboundComPortBuilder postDialCommands(String postDialCommands);
    }

}