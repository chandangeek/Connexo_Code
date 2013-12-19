package com.energyict.mdc.engine.model.impl;

import com.elster.jupiter.orm.DataMapper;
import com.energyict.mdc.common.TranslatableApplicationException;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.ComPortPool;
import com.energyict.mdc.protocol.api.ComPortType;
import com.google.common.collect.Iterables;
import java.util.Date;
import java.util.List;

/**
 * Serves as the root for all concrete {@link com.energyict.mdc.engine.model.ComPortPool} interfaces.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-26 (08:47)
 */
public abstract class ComPortPoolImpl implements ComPortPool {

    private long id;
    private String name;
    private boolean active;
    private String description;
    private boolean obsoleteFlag;
    private Date obsoleteDate;
    private ComPortType comPortType;

    protected ComPortPoolImpl() {
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
            List<ComPortPool> comPortPools = getComPortPoolFactory().find("id", this.getId());
            if (comPortPools != null && !comPortPools.isEmpty()) {
                this.obsoleteDate = Iterables.getOnlyElement(comPortPools).getObsoleteDate();
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
        List<ComPortPool> comPortPools = getComPortPoolFactory().find("name", this.getName());
        if (comPortPools != null && !comPortPools.isEmpty()) {
            for (ComPortPool comPortPool : comPortPools) {
                if (!comPortPool.isObsolete()) {
                    throw new TranslatableApplicationException("duplicateComPortPoolX", "A ComPortPool by the name of \"{0}\" already exists", name);
                }
            }
        }
    }

    private DataMapper<ComPortPool> getComPortPoolFactory() {
        return Bus.getServiceLocator().getOrmClient().getComPortPoolFactory();
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
            getComPortPoolFactory().persist(this);
        } else {
            validateUpdateAllowed();
            getComPortPoolFactory().update(this);
        }
    }

    protected void validateDelete() {
        // NO-OP so far
    }
}