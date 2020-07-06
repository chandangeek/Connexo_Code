/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.config.impl;

import com.elster.jupiter.domain.util.AllowedChars;
import com.elster.jupiter.domain.util.HasOnlyWhiteListedCharacters;
import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.common.TranslatableApplicationException;
import com.energyict.mdc.common.comserver.ComPort;
import com.energyict.mdc.common.comserver.ComPortPoolMember;
import com.energyict.mdc.common.comserver.ComServer;
import com.energyict.mdc.ports.ComPortType;

import com.google.common.collect.ImmutableMap;

import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.*;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Serves as the root of class hierarchy that will provide
 * an implementation for the {@link ComPort} interface hierarchy.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-02 (12:48)
 */
@UniqueName(groups = { Save.Create.class, Save.Update.class }, message = "{"+ MessageSeeds.Keys.MDC_DUPLICATE_COM_PORT+"}")
@XmlRootElement
public abstract class ComPortImpl implements ComPort {

    protected static final String MODEM_DISCRIMINATOR = "1";
    protected static final String TCP_DISCRIMINATOR = "2";
    protected static final String SERVLET_DISCRIMINATOR = "4";
    protected static final String UDP_DISCRIMINATOR = "3";
    protected static final String OUTBOUND_DISCRIMINATOR = "0";

    static final Map<String, Class<? extends ComPort>> IMPLEMENTERS =
            ImmutableMap.<String, Class<? extends ComPort>>of(
                    MODEM_DISCRIMINATOR, ModemBasedInboundComPortImpl.class,
                    TCP_DISCRIMINATOR, TCPBasedInboundComPortImpl.class,
                    SERVLET_DISCRIMINATOR, ServletBasedInboundComPortImpl.class,
                    UDP_DISCRIMINATOR, UDPBasedInboundComPortImpl.class,
                    OUTBOUND_DISCRIMINATOR, OutboundComPortImpl.class);
    private DataModel dataModel;
    protected Thesaurus thesaurus;

    enum FieldNames {
        NAME("name");
        private final String name;

        FieldNames(String name) {
            this.name = name;
        }

        String getName() {
            return name;
        }
    }

    private long id=0;
    @NotEmpty(groups = { Save.Create.class, Save.Update.class }, message = "{"+ MessageSeeds.Keys.MDC_CAN_NOT_BE_EMPTY+"}")
    @Size(max= Table.NAME_LENGTH, groups = { Save.Create.class, Save.Update.class }, message = "{"+MessageSeeds.Keys.MDC_FIELD_TOO_LONG+"}")
    @HasOnlyWhiteListedCharacters(whitelistRegex = AllowedChars.Constant.ALLOWED_CHARS_WITH_SPACE)
    private String name;
    @HasOnlyWhiteListedCharacters(whitelistRegex = AllowedChars.Constant.ALLOWED_CHARS_WITH_SPACE)
    private String userName;
    private long version;
    private Instant createTime;
    private Instant modTime;
    private final Reference<ComServer> comServer = ValueReference.absent();
    private boolean active;
    @Size(max= Table.NAME_LENGTH, groups = { Save.Create.class, Save.Update.class }, message = "{"+MessageSeeds.Keys.MDC_FIELD_TOO_LONG+"}")
    @HasOnlyWhiteListedCharacters(whitelistRegex = AllowedChars.Constant.ALLOWED_SPECIAL_CHARS)
    private String description;
    @Null(groups = { Save.Update.class }, message = "{"+ MessageSeeds.Keys.MDC_COMPORT_NO_UPDATE_ALLOWED+"}")
    private Instant obsoleteDate;
    @NotNull(groups = { Save.Create.class, Save.Update.class }, message = "{"+ MessageSeeds.Keys.MDC_CAN_NOT_BE_EMPTY+"}")
    private ComPortType type;
    protected boolean inbound;
    protected boolean obsolete;

    public ComPortImpl() {
        super();
    }

    @Inject
    protected ComPortImpl(DataModel dataModel, Thesaurus thesaurus) {
        this.dataModel = dataModel;
        this.thesaurus = thesaurus;
    }

    public void setName(String name) {
        this.name = name!=null?name.trim():null;
    }

    protected void validateCreate() {
        validate(Save.Create.class);
    }

    private void validate(Class<?> group) {
        Validator validator = ComPortImpl.this.dataModel.getValidatorFactory().getValidator();
        Set<ConstraintViolation<ComPortImpl>> constraintViolations = validator.validate(this, group);
        if (!constraintViolations.isEmpty()) {
            throw new ConstraintViolationException(constraintViolations);
        }
    }

    @XmlElement(name = "type")
    public String getXmlType () {
        return this.getClass().getSimpleName();
    }

    public void setXmlType (String ignore) {
        // For xml unmarshalling purposes only
    }

    @Override
    public Instant getModTime() {
        return this.modTime;
    }

    @Override
    @XmlAttribute
    public String getName() {
        return this.name;
    }

    @Override
    @XmlAttribute
    public boolean isActive() {
        return active;
    }

    @Override
    @XmlElement
    public String getDescription() {
        return description;
    }

    @Override
    @XmlAttribute
    public boolean isInbound() {
        return false;
    }

    @Override
    public int getNumberOfSimultaneousConnections() {
        return 0;
    }

    @Override
    public void setNumberOfSimultaneousConnections(int numberOfSimultaneousConnections) {

    }

    @Override
    @XmlTransient
    public ComServer getComServer() {
        return comServer.get();
    }

    @Override
    @XmlElement
    public boolean isObsolete() {
        obsolete = (this.obsoleteDate != null);
        return obsolete;
    }

    @Override
    @XmlElement
    public Instant getObsoleteDate() {
        return obsoleteDate;
    }

    @Override
    @XmlElement(name = "comPortType")
    public ComPortType getComPortType() {
        return type;
    }

    public void setComServer(ComServer comServer) {
        this.comServer.set(comServer);
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setComPortType(ComPortType type) {
        this.type = type;
    }

    @XmlAttribute
    public long getId() {
        return id;
    }

    @Override
    public void update() {
        if (this.getId()==0) {
            throw new IllegalStateException("ComPort should have been created using the ComServer, how did you end up here?");
        } else {
            Save.UPDATE.save(dataModel, this);
            this.comServer.get().saved(this);
        }
    }

    @Override
    public void makeObsolete(){
        this.validateMakeObsolete();
        this.obsoleteDate = Instant.now();
        this.removeFromComPortPools();
        dataModel.update(this);
    }

    @Override
    public long getVersion() {
        return this.version;
    }

    private void removeFromComPortPools() {
        DataMapper<ComPortPoolMember> comPortPoolMemberDataMapper = this.dataModel.mapper(ComPortPoolMember.class);
        List<ComPortPoolMember> comPortPoolMembers = comPortPoolMemberDataMapper.find("comPort", this);
        for (ComPortPoolMember comPortPoolMember : comPortPoolMembers) {
            comPortPoolMemberDataMapper.remove(comPortPoolMember);
        }
    }

    protected final void validateMakeObsolete() {
        if (this.obsoleteDate!=null) {
            throw new TranslatableApplicationException(thesaurus,MessageSeeds.IS_ALREADY_OBSOLETE);
        }
    }

    protected void copyFrom(ComPort source) {
        this.setName(source.getName());
        this.setActive(source.isActive());
        this.setDescription(source.getDescription());
    }

    @Override
    public String toString() {
        return getName();
    }

    protected static class ComPortBuilderImpl<B extends ComPort.Builder<B, C>, C extends ComPort> implements ComPort.Builder<B, C> {
        C comPort;
        B self;

        protected ComPortBuilderImpl(Class<B> clazz, C comPort, String name) {
            this.comPort = comPort;
            self = clazz.cast(this);
            this.comPort.setName(name);
        }

        @Override
        public B active(boolean active) {
            comPort.setActive(active);
            return self;
        }

        @Override
        public B description(String description) {
            comPort.setDescription(description);
            return self;
        }

        @Override
        public C add() {
            ((ComPortImpl)comPort).validateCreate();
            return comPort;
        }

    }

}