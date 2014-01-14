package com.energyict.mdc.engine.model.impl;

import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.common.TranslatableApplicationException;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.ComPortPool;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.protocol.api.ComPortType;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;

/**
 * Serves as the root for all concrete {@link com.energyict.mdc.engine.model.ComPortPool} interfaces.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-26 (08:47)
 */
public abstract class ComPortPoolImpl implements ComPortPool {
    protected static final String INBOUND_COMPORTPOOL_DISCRIMINATOR = "0";
    protected static final String OUTBOUND_COMPORTPOOL_DISCRIMINATOR = "1";

    static final Map<String, Class<? extends ComPortPool>> IMPLEMENTERS =
            ImmutableMap.<String, Class<? extends ComPortPool>>of(
                    INBOUND_COMPORTPOOL_DISCRIMINATOR, InboundComPortPoolImpl.class,
                    OUTBOUND_COMPORTPOOL_DISCRIMINATOR, OutboundComPortPoolImpl.class);
    private final DataModel dataModel;
    private final EngineModelService engineModelService;

    private long id;
    private String name;
    private boolean active;
    private String description;
    private boolean obsoleteFlag;
    private Date obsoleteDate;
    private ComPortType comPortType;

    @Inject
    protected ComPortPoolImpl(DataModel dataModel, EngineModelService engineModelService) {
        this.dataModel = dataModel;
        this.engineModelService = engineModelService;
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
        return obsoleteFlag;
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
        if (this.obsoleteFlag && this.obsoleteDate == null) {
            ComPortPool comPortPool = engineModelService.findComPortPool(this.getId());
            if (comPortPool != null) {
                this.obsoleteDate = comPortPool.getObsoleteDate();
            }
            else {
                this.obsoleteDate = null;
            }
        }
        return this.obsoleteDate;
    }

    @Override
    public ComPortType getComPortType () {
        return comPortType;
    }

    protected void validate() {
        this.validateNotNull(this.getComPortType(), "comportpool.comporttype");
    }

    protected void validateConstraint (String name) {
        ComPortPool comPortPool = engineModelService.findComPortPool(this.getName());
        if (comPortPool != null && comPortPool.getId()!=this.getId() && !comPortPool.isObsolete()) {
            throw new TranslatableApplicationException("duplicateComPortPoolX", "A ComPortPool by the name of \"{0}\" already exists", name);
        }
    }

    protected void validateNotNull (Object propertyValue, String propertyName) {
        if (propertyValue == null) {
            throw new TranslatableApplicationException("XcannotBeEmpty", "\"{0}\" is a required property", propertyName);
        }
    }

    @Override
    public void makeObsolete() {
        this.validateMakeObsolete();
        this.makeMembersObsolete();
        this.obsoleteFlag = true;
        this.save();
    }

    protected abstract void makeMembersObsolete ();

    protected void validateUpdateAllowed () {
        if (this.obsoleteFlag) {
            throw new TranslatableApplicationException("comportpool.noUpdateAllowed", "This comport pool is marked as deleted, no updates allowed.");
        }
    }

    protected void validateMakeObsolete () {
        if (this.isObsolete()) {
            throw new TranslatableApplicationException(
                    "comPortPoolIsAlreadyObsolete",
                    "The ComPortPool with id {0} is already obsolete since {1,date,yyyy-MM-dd HH:mm:ss}",
                    this.getId(),
                    this.getObsoleteDate());
        }
    }

    protected void validateComPortForComPortType(ComPort comPort, ComPortType comPortType) {
        if (!comPort.getComPortType().equals(comPortType)) {
            Object[] messageArguments = new Object[2];
            messageArguments[0] = comPort.getComPortType();
            messageArguments[1] = comPortType;
            throw new TranslatableApplicationException("comPortTypeOfComPortXDoesNotMatchWithComPortPoolY", "The ComPortType of ComPort {0} does not match with that of the ComPortPool {1}", messageArguments);
        }
    }

    @Override
    public void save() {
        validate();
        if (this.getId()==0) {
            dataModel.persist(this);
        } else {
            validateUpdateAllowed();
            dataModel.update(this);
        }
    }

    @Override
    public void delete() {
        validateDelete();
        dataModel.remove(this);
    }


    protected void validateDelete() {
        // NO-OP so far
    }
}