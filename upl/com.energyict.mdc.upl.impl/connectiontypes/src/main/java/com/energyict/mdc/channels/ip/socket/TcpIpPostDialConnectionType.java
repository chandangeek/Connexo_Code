package com.energyict.mdc.channels.ip.socket;

import com.energyict.mdc.channels.nls.PropertyTranslationKeys;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.upl.nls.TranslationKey;
import com.energyict.mdc.upl.properties.InvalidPropertyException;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;
import com.energyict.protocol.exception.ConnectionCommunicationException;
import com.energyict.protocol.exceptions.ConnectionException;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;

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

    public TcpIpPostDialConnectionType(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    public ComChannel connect() throws ConnectionException {
        ComChannel comChannel = this.newTcpIpConnection(this.hostPropertyValue(), this.portNumberPropertyValue(), this.connectionTimeOutPropertyValue());
        sendPostDialCommand(comChannel);
        return comChannel;
    }

    protected void sendPostDialCommand(ComChannel comChannel) {
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
            throw ConnectionCommunicationException.communicationInterruptedException(e);
        }
    }

    protected int getPostDialDelayPropertyValue() {
        BigDecimal postDialDelay = (BigDecimal) this.getProperty(POST_DIAL_DELAY);
        int delay;
        if (postDialDelay == null) {
            delay = DEFAULT_POST_DIAL_DELAY;
        } else {
            delay = postDialDelay.intValue();
        }
        return delay;
    }

    protected int getPostDialTriesPropertyValue() {
        BigDecimal postDialTries = (BigDecimal) this.getProperty(POST_DIAL_TRIES);
        int tries;
        if (postDialTries == null) {
            tries = DEFAULT_POST_DIAL_TRIES;
        } else {
            tries = postDialTries.intValue();
        }
        return tries;
    }

    protected String getPostDialCommandPropertyValue() {
        return (String) this.getProperty(POST_DIAL_COMMAND);
    }

    private PropertySpec postDialDelayPropertySpec() {
        return this.bigDecimalSpec(POST_DIAL_DELAY, PropertyTranslationKeys.OUTBOUND_IP_POST_DIAL_DELAY_MILLIS, BigDecimal.valueOf(DEFAULT_POST_DIAL_DELAY));
    }

    private PropertySpec postDialRetriesPropertySpec() {
        return this.bigDecimalSpec(POST_DIAL_TRIES, PropertyTranslationKeys.OUTBOUND_IP_POST_DIAL_COMMAND_ATTEMPTS, BigDecimal.valueOf(DEFAULT_POST_DIAL_TRIES));
    }

    private PropertySpec postDialCommandPropertySpec() {
        return UPLPropertySpecFactory.specBuilder(POST_DIAL_COMMAND, false, PropertyTranslationKeys.OUTBOUND_IP_POST_DIAL_COMMAND, this.getPropertySpecService()::stringSpec).finish();
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        List<PropertySpec> propertySpecs = super.getUPLPropertySpecs();
        propertySpecs.add(postDialDelayPropertySpec());
        propertySpecs.add(postDialRetriesPropertySpec());
        propertySpecs.add(postDialCommandPropertySpec());
        return propertySpecs;
    }

    @Override
    public void setUPLProperties(TypedProperties properties) throws PropertyValidationException {
        super.setUPLProperties(properties);
        this.validateProperties();
    }

    private void validateProperties() throws InvalidPropertyException {
        this.validatePostDialTriesProperty();
        this.validatePostDialDelayProperty();
    }

    private void validatePostDialTriesProperty() throws InvalidPropertyException {
        int postDialTriesPropertyValue = this.getPostDialTriesPropertyValue();
        if (postDialTriesPropertyValue < 0) {
            throw InvalidPropertyException.forNameAndValue(POST_DIAL_TRIES, postDialTriesPropertyValue);
        }
    }

    private void validatePostDialDelayProperty() throws InvalidPropertyException {
        int postDialDelayPropertyValue = this.getPostDialDelayPropertyValue();
        if (postDialDelayPropertyValue < 0) {
            throw InvalidPropertyException.forNameAndValue(POST_DIAL_DELAY, postDialDelayPropertyValue);
        }
    }

    @Override
    public String getVersion() {
        return "$Date: 2013-05-16 13:24:08 +0200 (do, 16 mei 2013) $";
    }

    private PropertySpec bigDecimalSpec(String name, TranslationKey translationKey, BigDecimal defaultValue) {
        return UPLPropertySpecFactory
                .specBuilder(name, false, translationKey, this.getPropertySpecService()::bigDecimalSpec)
                .setDefaultValue(defaultValue)
                .finish();
    }

}
