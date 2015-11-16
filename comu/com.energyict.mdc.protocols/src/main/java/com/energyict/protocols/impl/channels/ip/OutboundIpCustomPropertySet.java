package com.energyict.protocols.impl.channels.ip;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.ViewPrivilege;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.properties.BigDecimalFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.StringFactory;
import com.energyict.mdc.dynamic.TimeDurationValueFactory;
import com.energyict.mdc.protocol.api.ConnectionProvider;

import java.util.EnumSet;
import java.util.Set;

/**
 * Serves as the root for class that will implement the {@link CustomPropertySet} interface
 * for the {@link OutboundIpConnectionType} class hierarcy.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-06 (12:03)
 */
public abstract class OutboundIpCustomPropertySet implements CustomPropertySet<ConnectionProvider, OutboundIpConnectionProperties> {

    private final Thesaurus thesaurus;
    private final TranslationKey translationKey;
    private final PropertySpecService propertySpecService;

    public OutboundIpCustomPropertySet(Thesaurus thesaurus, TranslationKey translationKey, PropertySpecService propertySpecService) {
        super();
        this.thesaurus = thesaurus;
        this.translationKey = translationKey;
        this.propertySpecService = propertySpecService;
    }

    protected PropertySpecService getPropertySpecService() {
        return propertySpecService;
    }

    @Override
    public String getId() {
        return this.getClass().getName();
    }

    @Override
    public String getName() {
        return this.thesaurus.getFormat(this.translationKey).format();
    }

    @Override
    public Class<ConnectionProvider> getDomainClass() {
        return ConnectionProvider.class;
    }

    @Override
    public boolean isRequired() {
        return true;
    }

    @Override
    public boolean isVersioned() {
        return true;
    }

    @Override
    public Set<ViewPrivilege> defaultViewPrivileges() {
        return EnumSet.allOf(ViewPrivilege.class);
    }

    @Override
    public Set<EditPrivilege> defaultEditPrivileges() {
        return EnumSet.allOf(EditPrivilege.class);
    }

    protected PropertySpec hostPropertySpec() {
        return this.getPropertySpecService()
                .basicPropertySpec(
                        OutboundIpConnectionProperties.Fields.HOST.javaName(),
                        true,
                        new StringFactory());
    }

    protected PropertySpec portPropertySpec() {
        return this.getPropertySpecService()
                .basicPropertySpec(
                        OutboundIpConnectionProperties.Fields.PORT_NUMBER.javaName(),
                        true,
                        new BigDecimalFactory());
    }

    protected PropertySpec connectionTimeoutPropertySpec() {
        return this.getPropertySpecService()
                .basicPropertySpec(
                        OutboundIpConnectionProperties.Fields.CONNECTION_TIMEOUT.javaName(),
                        false,
                        new TimeDurationValueFactory());
    }

    protected PropertySpec bufferSizePropertySpec() {
        return this.getPropertySpecService()
                .basicPropertySpec(
                        OutboundIpConnectionProperties.Fields.CONNECTION_TIMEOUT.javaName(),
                        true,
                        new BigDecimalFactory());
    }

    protected PropertySpec postDialMillisPropertySpec() {
        return this.getPropertySpecService()
                .basicPropertySpec(
                        OutboundIpConnectionProperties.Fields.POST_DIAL_DELAY_MILLIS.javaName(),
                        false,
                        new BigDecimalFactory());
    }

    protected PropertySpec postDialCommandAttempsPropertySpec() {
        return this.getPropertySpecService()
                .basicPropertySpec(
                        OutboundIpConnectionProperties.Fields.POST_DIAL_COMMAND_ATTEMPTS.javaName(),
                        false,
                        new BigDecimalFactory());
    }

    protected PropertySpec postDialCommandPropertySpec() {
        return this.getPropertySpecService()
                .basicPropertySpec(
                        OutboundIpConnectionProperties.Fields.POST_DIAL_COMMAND.javaName(),
                        false,
                        new StringFactory());
    }

}