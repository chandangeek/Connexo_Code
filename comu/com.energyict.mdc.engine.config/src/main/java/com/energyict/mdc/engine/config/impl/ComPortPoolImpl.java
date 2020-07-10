/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.config.impl;

import com.elster.jupiter.domain.util.HasNoBlacklistedCharacters;
import com.elster.jupiter.domain.util.HasNotAllowedChars;
import com.elster.jupiter.domain.util.NotEmpty;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.energyict.mdc.common.TranslatableApplicationException;
import com.energyict.mdc.common.comserver.ComPort;
import com.energyict.mdc.common.comserver.ComPortPool;
import com.energyict.mdc.ports.ComPortType;

import com.google.common.collect.ImmutableMap;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAttribute;
import java.time.Instant;
import java.util.Map;


/**
 * Serves as the root for all concrete {@link ComPortPool} interfaces.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-26 (08:47)
 */
@UniqueName(groups = {Save.Create.class, Save.Update.class}, message = "{"+ MessageSeeds.Keys.MDC_DUPLICATE_COM_PORT_POOL+"}")
public abstract class ComPortPoolImpl implements ComPortPool {
    protected static final String INBOUND_COMPORTPOOL_DISCRIMINATOR = "0";
    protected static final String OUTBOUND_COMPORTPOOL_DISCRIMINATOR = "1";

    enum Fields {
        // The field definitions below must match the exact name of the field in the class.
        NAME("name"),
        ACTIVE("active"),
        DESCRIPTION("description"),
        OBSOLETEDATE("obsoleteDate"),
        COMPORTTYPE("comPortType"),
        PCTHIGHPRIOTASKS("pctHighPrioTasks");
        private final String name;

        Fields(String name) {
            this.name = name;
        }

        String fieldName() {
            return name;
        }
    }

    static final Map<String, Class<? extends ComPortPool>> IMPLEMENTERS =
            ImmutableMap.<String, Class<? extends ComPortPool>>of(
                    INBOUND_COMPORTPOOL_DISCRIMINATOR, InboundComPortPoolImpl.class,
                    OUTBOUND_COMPORTPOOL_DISCRIMINATOR, OutboundComPortPoolImpl.class);

    protected DataModel dataModel;
    protected Thesaurus thesaurus;
    protected EventService eventService;

    private long id;
    @NotEmpty(groups = {Save.Create.class, Save.Update.class}, message = "{"+ MessageSeeds.Keys.MDC_CAN_NOT_BE_EMPTY+"}")
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{"+ MessageSeeds.Keys.MDC_FIELD_TOO_LONG+"}")
    @HasNoBlacklistedCharacters(balcklistedCharRegEx = HasNotAllowedChars.Constant.SPECIAL_CHARS)
    private String name;
    private boolean active;
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{"+ MessageSeeds.Keys.MDC_FIELD_TOO_LONG+"}")
    private String description;
    @Null(groups = { Save.Update.class }, message = "{"+ MessageSeeds.Keys.MDC_COMPORTPOOL_NO_UPDATE_ALLOWED+"}")
    private Instant obsoleteDate;
    @NotNull(groups = {Save.Create.class, Save.Update.class}, message = "{"+ MessageSeeds.Keys.MDC_CAN_NOT_BE_EMPTY+"}")
    private ComPortType comPortType;
    private String userName;
    private long version;
    private Instant createTime;
    private Instant modTime;
    private boolean obsolete;
    private long pctHighPrioTasks;

    protected ComPortPoolImpl() {
        super();
    }


    @Inject
    protected ComPortPoolImpl(DataModel dataModel, Thesaurus thesaurus, EventService eventService) {
        this.dataModel = dataModel;
        this.thesaurus = thesaurus;
        this.eventService = eventService;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    @XmlAttribute
    public String getName() {
        return name;
    }

    @Override
    @XmlAttribute
    public boolean isActive () {
        return active;
    }

    @Override
    @XmlAttribute
    public String getDescription () {
        return description;
    }

    @Override
    @XmlAttribute
    public boolean isObsolete () {
        obsolete = this.obsoleteDate!=null;
        return obsolete;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    @XmlAttribute
    public Instant getObsoleteDate () {
        return this.obsoleteDate;
    }

    @Override
    @XmlAttribute
    public ComPortType getComPortType () {
        return comPortType;
    }

    protected void setComPortType(ComPortType comPortType) {
        this.comPortType = comPortType;
    }

    protected void validate() {
    }

    @Override
    public void makeObsolete() {
        this.validateMakeObsolete();
        this.makeMembersObsolete();
        this.obsoleteDate = Instant.now();
        dataModel.update(this);
    }

    protected abstract void makeMembersObsolete();

    protected void validateMakeObsolete () {
        if (this.isObsolete()) {
            throw new TranslatableApplicationException(thesaurus,MessageSeeds.IS_ALREADY_OBSOLETE);
        }
        this.validateNotUsed();
    }

    protected void validateComPortForComPortType(ComPort comPort, ComPortType comPortType) {
        if (!comPort.getComPortType().equals(comPortType)) {
            throw new TranslatableApplicationException(thesaurus,MessageSeeds.COMPORTPOOL_DOES_NOT_MATCH_COMPORT);
        }
    }

    public void save() {
        Save.action(getId()).save(dataModel, this);
    }

    @Override
    public void delete() {
        this.validateDelete();
        dataModel.remove(this);
    }

    protected void validateDelete() {
        this.validateNotUsed();
    }

    private void validateNotUsed() {
        this.eventService.postEvent(EventType.COMPORTPOOL_VALIDATE_DELETE.topic(), this);
    }

    @Override
    @XmlAttribute
    public long getVersion() {
        return this.version;
    }
    
    @Override
    public void update() {
        Save.action(getId()).save(dataModel, this);
    }
    
    

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ComPortPoolImpl that = (ComPortPoolImpl) o;

        return id == that.id;

    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    @Override
    public long getPctHighPrioTasks() { return this.pctHighPrioTasks;}

    @Override
    public void setPctHighPrioTasks(long pctHighPrioTasks) { this.pctHighPrioTasks = pctHighPrioTasks;}
}