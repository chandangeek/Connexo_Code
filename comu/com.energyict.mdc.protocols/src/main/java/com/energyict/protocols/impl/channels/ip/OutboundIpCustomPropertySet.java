/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocols.impl.channels.ip;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.ViewPrivilege;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.energyict.mdc.dynamic.TimeDurationValueFactory;
import com.energyict.mdc.protocol.api.ConnectionProvider;
import com.energyict.protocols.impl.channels.TranslationKeys;
import com.energyict.protocols.naming.ConnectionTypePropertySpecName;

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
    public String getDomainClassDisplayName() {
        return this.thesaurus.getFormat(TranslationKeys.CONNECTION_PROVIDER_DOMAIN_NAME).format();
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
                .stringSpec()
                .named(OutboundIpConnectionProperties.Fields.HOST.propertySpecName(), ConnectionTypePropertySpecName.OUTBOUND_IP_HOST)
                .fromThesaurus(this.thesaurus)
                .markRequired()
                .finish();
    }

    protected PropertySpec portPropertySpec() {
        return this.getPropertySpecService()
                .bigDecimalSpec()
                .named(OutboundIpConnectionProperties.Fields.PORT_NUMBER.propertySpecName(), ConnectionTypePropertySpecName.OUTBOUND_IP_PORT_NUMBER)
                .fromThesaurus(this.thesaurus)
                .markRequired()
                .finish();
    }

    protected PropertySpec connectionTimeoutPropertySpec() {
        return this.getPropertySpecService()
                .specForValuesOf(new TimeDurationValueFactory())
                .named(OutboundIpConnectionProperties.Fields.CONNECTION_TIMEOUT.propertySpecName(), ConnectionTypePropertySpecName.OUTBOUND_IP_CONNECTION_TIMEOUT)
                .fromThesaurus(this.thesaurus)
                .setDefaultValue(OutboundIpConnectionType.DEFAULT_CONNECTION_TIMEOUT)
                .finish();
    }

    protected PropertySpec bufferSizePropertySpec() {
        return this.getPropertySpecService()
                .bigDecimalSpec()
                .named(OutboundIpConnectionProperties.Fields.BUFFER_SIZE.propertySpecName(), ConnectionTypePropertySpecName.OUTBOUND_IP_BUFFER_SIZE)
                .fromThesaurus(this.thesaurus)
                .markRequired()
                .finish();
    }

    protected PropertySpec postDialDelayMillisPropertySpec() {
        return this.getPropertySpecService()
                .bigDecimalSpec()
                .named(OutboundIpConnectionProperties.Fields.POST_DIAL_DELAY_MILLIS.propertySpecName(), ConnectionTypePropertySpecName.OUTBOUND_IP_POST_DIAL_DELAY_MILLIS)
                .fromThesaurus(this.thesaurus)
                .finish();
    }

    protected PropertySpec postDialCommandAttempsPropertySpec() {
        return this.getPropertySpecService()
                .bigDecimalSpec()
                .named(OutboundIpConnectionProperties.Fields.POST_DIAL_COMMAND_ATTEMPTS.propertySpecName(), ConnectionTypePropertySpecName.OUTBOUND_IP_POST_DIAL_COMMAND_ATTEMPTS)
                .fromThesaurus(this.thesaurus)
                .finish();
    }

    protected PropertySpec postDialCommandPropertySpec() {
        return this.getPropertySpecService()
                .stringSpec()
                .named(OutboundIpConnectionProperties.Fields.POST_DIAL_COMMAND.propertySpecName(), ConnectionTypePropertySpecName.OUTBOUND_IP_POST_DIAL_COMMAND)
                .fromThesaurus(this.thesaurus)
                .finish();
    }

}