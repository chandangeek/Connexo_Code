package com.energyict.protocols.impl.channels.ip.socket;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.BigDecimalFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.StringFactory;
import com.energyict.mdc.common.InvalidValueException;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.io.ComChannel;
import com.energyict.mdc.io.SocketService;
import com.energyict.mdc.protocol.api.ConnectionException;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.dynamic.ConnectionProperty;
import com.energyict.protocols.impl.channels.ip.OutboundIpConnectionProperties;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * @author sva
 * @since 18/03/13 - 13:25
 */
public class TcpIpPostDialConnectionType extends OutboundTcpIpConnectionType {

    /**
     * The default post dial delay is 500 ms
     */
    protected static final int DEFAULT_POST_DIAL_DELAY = 500;

    /**
     * The default post dial number of tries is set to 1, so send only once and do not repeat
     */
    protected static final int DEFAULT_POST_DIAL_TRIES = 1;

    @Inject
    public TcpIpPostDialConnectionType(Thesaurus thesaurus, PropertySpecService propertySpecService, SocketService socketService) {
        super(thesaurus, propertySpecService, socketService);
    }

    @Override
    public ComChannel connect(List<ConnectionProperty> properties) throws ConnectionException {
        this.copyProperties(properties);
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
        BigDecimal postDialDelay = (BigDecimal) this.getProperty(OutboundIpConnectionProperties.Fields.POST_DIAL_DELAY_MILLIS.javaName());
        int delay;
        if (postDialDelay == null) {
            delay = DEFAULT_POST_DIAL_DELAY;
        }
        else {
            // Note that java.validation on the OutboundIpConnectionProperties takes care of validating that value >= 0
            delay = postDialDelay.intValue();
        }
        return delay;
    }

    protected int getPostDialTriesPropertyValue() throws InvalidValueException {
        BigDecimal postDialTries = (BigDecimal) this.getProperty(OutboundIpConnectionProperties.Fields.POST_DIAL_COMMAND_ATTEMPTS.javaName());
        int tries;
        if (postDialTries == null) {
            tries = DEFAULT_POST_DIAL_TRIES;
        }
        else {
            // java.validation on the OutboundIpConnectionProperties takes care of validating that value >= 0
            tries = postDialTries.intValue();
        }
        return tries;
    }

    protected String getPostDialCommandPropertyValue() {
        return (String) this.getProperty(OutboundIpConnectionProperties.Fields.POST_DIAL_COMMAND.javaName());
    }

    private PropertySpec postDialDelayPropertySpec() {
        return this.getPropertySpecService().basicPropertySpec(OutboundIpConnectionProperties.Fields.POST_DIAL_DELAY_MILLIS.javaName(), false, new BigDecimalFactory());
    }

    private PropertySpec postDialRetriesPropertySpec() {
        return this.getPropertySpecService().basicPropertySpec(OutboundIpConnectionProperties.Fields.POST_DIAL_COMMAND_ATTEMPTS.javaName(), false, new BigDecimalFactory());
    }

    private PropertySpec postDialCommandPropertySpec() {
        return this.getPropertySpecService().basicPropertySpec(OutboundIpConnectionProperties.Fields.POST_DIAL_COMMAND.javaName(), false, new StringFactory());
    }

    @Override
    public Optional<CustomPropertySet<ConnectionType, ? extends PersistentDomainExtension<ConnectionType>>> getCustomPropertySet() {
        return Optional.of(new TcpIpPostDialCustomPropertySet(this.getThesaurus(), this.getPropertySpecService()));
    }

    @Override
    public String getVersion() {
        return "$Date: 2013-05-16 13:24:08 +0200 (do, 16 mei 2013) $";
    }

}