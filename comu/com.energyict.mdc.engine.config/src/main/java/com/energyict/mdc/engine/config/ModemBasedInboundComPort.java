package com.energyict.mdc.engine.config;

import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.io.SerialPortConfiguration;
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
    public int getRingCount();
    public void setRingCount(int ringCount);

    public int getMaximumDialErrors();
    public void setMaximumDialErrors(int max);

    public TimeDuration getConnectTimeout();
    public void setConnectTimeout(TimeDuration connectTimeout);

    public TimeDuration getDelayAfterConnect();
    public void setDelayAfterConnect(TimeDuration connectTimeout);

    public TimeDuration getDelayBeforeSend();
    public void setDelayBeforeSend(TimeDuration connectTimeout);

    public TimeDuration getAtCommandTimeout();
    public void setAtCommandTimeout(TimeDuration connectTimeout);

    public BigDecimal getAtCommandTry();
    public void setAtCommandTry(BigDecimal atCommandTry);

    public List<String> getModemInitStrings();
    public void setModemInitStrings(List<String> initStrings);

    public List<String> getGlobalModemInitStrings();
    public void setGlobalModemInitStrings(List<String> globalModemInitStrings);

    public String getAddressSelector();
    public void setAddressSelector(String addressSelector);

    public String getPostDialCommands();
    public void setPostDialCommands(String postDialCommands);

    public SerialPortConfiguration getSerialPortConfiguration();
    public void setSerialPortConfiguration(SerialPortConfiguration serialPortConfiguration);

    interface ModemBasedInboundComPortBuilder extends InboundComPortBuilder<ModemBasedInboundComPortBuilder, ModemBasedInboundComPort>{
        public ModemBasedInboundComPortBuilder delayAfterConnect(TimeDuration delayAfterConnect);
        public ModemBasedInboundComPortBuilder delayBeforeSend(TimeDuration delayBeforeSend);
        public ModemBasedInboundComPortBuilder atCommandTry(BigDecimal atCommandTry);
        public ModemBasedInboundComPortBuilder atModemInitStrings(List<String> initStrings);
        public ModemBasedInboundComPortBuilder globalAtModemInitStrings(List<String> globalAtModemInitStrings);
        public ModemBasedInboundComPortBuilder addressSelector(String addressSelector);
        public ModemBasedInboundComPortBuilder postDialCommands(String postDialCommands);
    }

}