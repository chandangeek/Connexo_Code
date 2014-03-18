package com.energyict.mdc.engine.model.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.common.TranslatableApplicationException;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.ComPortPool;
import com.energyict.mdc.protocol.api.ComPortType;
import com.google.common.collect.ImmutableMap;
import java.util.Date;
import java.util.Map;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Size;

/**
 * Serves as the root for all concrete {@link com.energyict.mdc.engine.model.ComPortPool} interfaces.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-26 (08:47)
 */
@UniqueName(groups = {Save.Create.class, Save.Update.class}, message = "{"+Constants.MDC_DUPLICATE_COM_PORT_POOL+"}")
public abstract class ComPortPoolImpl implements ComPortPool {
    protected static final String INBOUND_COMPORTPOOL_DISCRIMINATOR = "0";
    protected static final String OUTBOUND_COMPORTPOOL_DISCRIMINATOR = "1";

    // The field definitions below must match the exact name of the field in the class.
    public static final String FIELD_NAME = "name";
    public static final String FIELD_ACTIVE = "active";
    public static final String FIELD_DESCRIPTION = "description";
    public static final String FIELD_OBSOLETEDATE = "obsoleteDate";
    public static final String FIELD_COMPORTTYPE = "comPortType";

    static final Map<String, Class<? extends ComPortPool>> IMPLEMENTERS =
            ImmutableMap.<String, Class<? extends ComPortPool>>of(
                    INBOUND_COMPORTPOOL_DISCRIMINATOR, InboundComPortPoolImpl.class,
                    OUTBOUND_COMPORTPOOL_DISCRIMINATOR, OutboundComPortPoolImpl.class);

    private final DataModel dataModel;
    protected final Thesaurus thesaurus;

    private long id;
    @NotNull(groups = {Save.Create.class, Save.Update.class}, message = "{"+Constants.MDC_CAN_NOT_BE_EMPTY+"}")
    @Size(min = 1, groups = {Save.Create.class, Save.Update.class}, message = "{"+Constants.MDC_CAN_NOT_BE_EMPTY+"}")
    private String name;
    private boolean active;
    private String description;
    @Null(groups = { Save.Update.class }, message = "{"+Constants.MDC_COMPORTPOOL_NO_UPDATE_ALLOWED+"}")
    private Date obsoleteDate;
    @NotNull(groups = {Save.Create.class, Save.Update.class}, message = "{"+Constants.MDC_CAN_NOT_BE_EMPTY+"}")
    private ComPortType comPortType;

    @Inject
    protected ComPortPoolImpl(DataModel dataModel, Thesaurus thesaurus) {
        this.dataModel = dataModel;
        this.thesaurus = thesaurus;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isActive () {
        return active;
    }

    @Override
    public String getDescription () {
        return description;
    }

    @Override
    public boolean isObsolete () {
        return this.obsoleteDate!=null;
    }

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
    public void setComPortType(ComPortType comPortType) {
        this.comPortType = comPortType;
    }

    @Override
    public Date getObsoleteDate () {
        return this.obsoleteDate;
    }

    @Override
    public ComPortType getComPortType () {
        return comPortType;
    }

    protected void validate() {
    }

    @Override
    public void makeObsolete() {
        this.validateMakeObsolete();
        this.makeMembersObsolete();
        this.obsoleteDate = new Date();
        dataModel.update(this);
    }

    protected abstract void makeMembersObsolete();

    protected void validateMakeObsolete () {
        if (this.isObsolete()) {
            throw new TranslatableApplicationException(thesaurus,MessageSeeds.IS_ALREADY_OBSOLETE);
        }
    }

    protected void validateComPortForComPortType(ComPort comPort, ComPortType comPortType) {
        if (!comPort.getComPortType().equals(comPortType)) {
            throw new TranslatableApplicationException(thesaurus,MessageSeeds.COMPORTPOOL_DOES_NOT_MATCH_COMPORT);
        }
    }

    @Override
    public void save() {
        Save.action(getId()).save(dataModel, this);
    }

    @Override
    public void delete() {
        validateDelete();
        dataModel.remove(this);
    }

    protected abstract void validateDelete();
}