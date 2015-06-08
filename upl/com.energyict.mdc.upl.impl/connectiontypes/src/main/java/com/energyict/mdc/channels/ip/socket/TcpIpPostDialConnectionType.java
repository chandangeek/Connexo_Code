package com.energyict.mdc.channels.ip.socket;

import com.energyict.cbo.InvalidValueException;
import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.mdc.channels.ComChannelType;
import com.energyict.mdc.ports.ComPort;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.ConnectionException;
import com.energyict.mdc.protocol.ServerLoggableComChannel;
import com.energyict.mdc.tasks.ConnectionTaskProperty;
import com.energyict.protocolimplv2.MdcManager;

import javax.xml.bind.annotation.XmlRootElement;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author sva
 * @since 18/03/13 - 13:25
 */
@XmlRootElement
public class TcpIpPostDialConnectionType extends OutboundTcpIpConnectionType {

    /**
     * The delay, expressed in milliseconds, to wait (after the connect) before sending the post dial command
     */
    public static final String POST_DIAL_DELAY = "postDialDelay";

    /**
     * The number of tries the post dial command must be transmitted. E.g.: if set to 3, the post dial command will be transferred three times in a row.
     * By doing so, we increase probability the receiver has received at least once the post dial command
     */
    public static final String POST_DIAL_TRIES = "postDialTries";

    /**
     * The post dial command that must to be send right after the connection has been established.
     */
    public static final String POST_DIAL_COMMAND = "postDialCommand";

    /**
     * The default post dial delay is 500 ms
     */
    protected static final int DEFAULT_POST_DIAL_DELAY = 500;

    /**
     * The default post dial number of tries is set to 1, so send only once and do not repeat
     */
    protected static final int DEFAULT_POST_DIAL_TRIES = 1;

    @Override
    public ComChannel connect(ComPort comPort, List<ConnectionTaskProperty> properties) throws ConnectionException {
        for (ConnectionTaskProperty property : properties) {
            if (property.getValue() != null) {
                this.setProperty(property.getName(), property.getValue());
            }
        }
        try {
            ServerLoggableComChannel comChannel = this.newTcpIpConnection(this.hostPropertyValue(), this.portNumberPropertyValue(), this.connectionTimeOutPropertyValue());
            comChannel.addProperties(createTypeProperty(ComChannelType.SocketComChannel));
            comChannel.setComPort(comPort);
            sendPostDialCommand(comChannel);
            return comChannel;
        } catch (InvalidValueException e) {
            throw new ConnectionException(e);
        }
    }

    protected void sendPostDialCommand(ServerLoggableComChannel comChannel) throws InvalidValueException {
        if (getPostDialCommandPropertyValue() != null) {
            for (int i = 0; i < getPostDialTriesPropertyValue(); i++) {
                delayBeforeSend(getPostDialDelayPropertyValue());
                comChannel.startWriting();
                comChannel.write(getPostDialCommandPropertyValue().getBytes());
            }
        }
    }

    private void delayBeforeSend(long milliSecondsToSleep) {
        try {
            Thread.sleep(milliSecondsToSleep);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw MdcManager.getComServerExceptionFactory().communicationInterruptedException(e);
        }
    }

    protected int getPostDialDelayPropertyValue() throws InvalidValueException {
        BigDecimal postDialDelay = (BigDecimal) this.getProperty(POST_DIAL_DELAY);
        int delay;
        if (postDialDelay == null) {
            delay = DEFAULT_POST_DIAL_DELAY;
        } else {
            delay = postDialDelay.intValue();
        }

        if (delay < 0) {
            throw new InvalidValueException("XcannotBeEqualOrLessThanZero", "\"{0}\" should have a value greater then 0", POST_DIAL_DELAY);
        }
        return delay;
    }

    protected int getPostDialTriesPropertyValue() throws InvalidValueException {
        BigDecimal postDialTries = (BigDecimal) this.getProperty(POST_DIAL_TRIES);
        int tries;
        if (postDialTries == null) {
            tries = DEFAULT_POST_DIAL_TRIES;
        } else {
            tries = postDialTries.intValue();
        }

        if (tries < 0) {
            throw new InvalidValueException("XcannotBeEqualOrLessThanZero", "\"{0}\" should have a value greater then 0", POST_DIAL_TRIES);
        }
        return tries;
    }

    protected String getPostDialCommandPropertyValue() {
        return (String) this.getProperty(POST_DIAL_COMMAND);
    }

    private PropertySpec postDialDelayPropertySpec() {
        return PropertySpecFactory.bigDecimalPropertySpec(POST_DIAL_DELAY, BigDecimal.valueOf(DEFAULT_POST_DIAL_DELAY));
    }

    private PropertySpec postDialRetriesPropertySpec() {
        return PropertySpecFactory.bigDecimalPropertySpec(POST_DIAL_TRIES, BigDecimal.valueOf(DEFAULT_POST_DIAL_TRIES));
    }

    private PropertySpec postDialCommandPropertySpec() {
        return PropertySpecFactory.stringPropertySpec(POST_DIAL_COMMAND);
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        final List<PropertySpec> allOptionalProperties = super.getOptionalProperties();
        allOptionalProperties.add(this.postDialDelayPropertySpec());
        allOptionalProperties.add(this.postDialRetriesPropertySpec());
        allOptionalProperties.add(this.postDialCommandPropertySpec());
        return allOptionalProperties;
    }

    @Override
    public PropertySpec getPropertySpec(String name) {
        PropertySpec superPropertySpec = super.getPropertySpec(name);
        if (superPropertySpec != null) {
            return superPropertySpec;
        } else if (POST_DIAL_DELAY.equals(name)) {
            return this.postDialDelayPropertySpec();
        } else if (POST_DIAL_TRIES.equals(name)) {
            return this.postDialRetriesPropertySpec();
        } else if (POST_DIAL_COMMAND.equals(name)) {
            return this.postDialCommandPropertySpec();
        } else {
            return null;
        }
    }

    @Override
    public String getVersion() {
        return "$Date: 2013-05-16 13:24:08 +0200 (do, 16 mei 2013) $";
    }
}
