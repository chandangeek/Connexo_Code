package com.energyict.protocols.impl.channels.ip.socket;

import com.energyict.mdc.common.InvalidValueException;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.io.ComChannel;
import com.energyict.mdc.io.SocketService;
import com.energyict.mdc.protocol.api.ConnectionException;
import com.energyict.mdc.protocol.api.dynamic.ConnectionProperty;

import com.elster.jupiter.properties.BigDecimalFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.StringFactory;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author sva
 * @since 18/03/13 - 13:25
 */
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

    @Inject
    public TcpIpPostDialConnectionType(PropertySpecService propertySpecService, SocketService socketService) {
        super(propertySpecService, socketService);
    }

    @Override
    public ComChannel connect(List<ConnectionProperty> properties) throws ConnectionException {
        properties
                .stream()
                .filter(property -> property.getValue() != null)
                .forEach(property -> this.setProperty(property.getName(), property.getValue()));
        try {
            ComChannel comChannel = this.newTcpIpConnection(this.getSocketService(), this.hostPropertyValue(), this.portNumberPropertyValue(), this.connectionTimeOutPropertyValue());
            sendPostDialCommand(comChannel);
            return comChannel;
        }
        catch (InvalidValueException e) {
            throw new ConnectionException(e);
        }
    }

    protected void sendPostDialCommand(ComChannel comChannel) throws InvalidValueException {
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
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    protected int getPostDialDelayPropertyValue() throws InvalidValueException {
        BigDecimal postDialDelay = (BigDecimal) this.getProperty(POST_DIAL_DELAY);
        int delay;
        if (postDialDelay == null) {
            delay = DEFAULT_POST_DIAL_DELAY;
        }
        else {
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
        }
        else {
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
        return this.getPropertySpecService().basicPropertySpec(POST_DIAL_DELAY, false, new BigDecimalFactory());
    }

    private PropertySpec postDialRetriesPropertySpec() {
        return this.getPropertySpecService().basicPropertySpec(POST_DIAL_TRIES, false, new BigDecimalFactory());
    }

    private PropertySpec postDialCommandPropertySpec() {
        return this.getPropertySpecService().basicPropertySpec(POST_DIAL_COMMAND, false, new StringFactory());
    }

    @Override
    @Obsolete
    public List<PropertySpec> getPropertySpecs() {
        List<PropertySpec> propertySpecs = new ArrayList<>(super.getPropertySpecs());
        propertySpecs.add(this.postDialDelayPropertySpec());
        propertySpecs.add(this.postDialRetriesPropertySpec());
        propertySpecs.add(this.postDialCommandPropertySpec());
        return propertySpecs;
    }

    @Override
    public String getVersion() {
        return "$Date: 2013-05-16 13:24:08 +0200 (do, 16 mei 2013) $";
    }

}