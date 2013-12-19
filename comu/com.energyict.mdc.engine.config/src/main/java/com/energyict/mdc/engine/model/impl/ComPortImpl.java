package com.energyict.mdc.engine.model.impl;

import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.Checks;
import com.energyict.mdc.common.InvalidValueException;
import com.energyict.mdc.common.TranslatableApplicationException;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.ComPortPool;
import com.energyict.mdc.engine.model.ComPortPoolMember;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.protocol.api.ComPortType;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Serves as the root of class hierarchy that will provide
 * an implementation for the {@link com.energyict.mdc.engine.model.ComPort} interface hierarchy.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-02 (12:48)
 */
public abstract class ComPortImpl implements ServerComPort {
    static final Map<String, Class<? extends ServerComPort>> IMPLEMENTERS =
            ImmutableMap.<String, Class<? extends ServerComPort>>of(
                    "0", ModemBasedInboundComPortImpl.class,
                    "1", TCPBasedInboundComPortImpl.class,
                    "2", ServletBasedInboundComPortImpl.class,
                    "3", UDPBasedInboundComPortImpl.class,
                    "5", OutboundComPortImpl.class);

    private long id=0;
    private String name;
    private Date modificationDate;
    private Reference<ComServer> comServer;
    private boolean active;
    private String description;
    private boolean obsoleteFlag;
    private Date obsoleteDate;
    private ComPortType type;
    private final List<ComPortPoolMember> comPortPoolMembers = new ArrayList<>();

    protected ComPortImpl() {
    }

    protected ComPortImpl(ComServer comServer) {
        this.comServer = ValueReference.of(comServer);
    }

    public void setName(String name) {
        this.validateName();
        this.name = name;
    }

    protected void validate() {
        Objects.requireNonNull(this.name);
        Objects.requireNonNull(this.type);
        validateName();
    }

    private void validateUpdateAllowed() {
        if (this.obsoleteFlag) {
            throw new TranslatableApplicationException("comport.noUpdateAllowed", "This comport is marked as deleted, no updates allowed.");
        }
    }

    private void validateName()  {
        for (ComPort comPort : comServer.get().getComPorts()) {
            if (comPort!=this && comPort.getName().equals(this.name)) {
                throw new TranslatableApplicationException("duplicateComPortX", "A ComPort by the name of \"{0}\" already exists", this.name);
            }
        }
    }

    protected void validateNotNull(Object propertyValue, String propertyName) {
        if (propertyValue == null) {
            throw new TranslatableApplicationException("XcannotBeEmpty", "\"{0}\" is a required property", propertyName);
        }
    }

    protected void validateNotNull(String propertyValue, String propertyName) {
        if (Checks.is(propertyValue).emptyOrOnlyWhiteSpace()) {
            throw new TranslatableApplicationException("XcannotBeEmpty", "\"{0}\" is a required property", propertyName);
        }
    }

    /**
     * Validate if the given propertyValue is greater then 0
     *
     * @param propertyValue the value to check
     * @param propertyName  the name of the property which is validated
     * @throws InvalidValueException if the given value is equal or below zero
     */
    protected void validateGreaterThanZero(int propertyValue, String propertyName) {
        if (propertyValue <= 0) {
            throw new TranslatableApplicationException("XcannotBeEqualOrLessThanZero", "\"{0}\" should have a value greater then 0", propertyName);
        }
    }

    protected void validateInRange(Range<Integer> acceptableRange, int propertyValue, String propertyName) {
        if (!acceptableRange.contains(propertyValue)) {
            throw new TranslatableApplicationException("XnotInAcceptableRange", "\"{0}\" should be between {1} and {2}", propertyName, acceptableRange.lowerEndpoint(), acceptableRange.upperEndpoint());
        }
    }

    @Override
    public void makeObsolete(){
        this.validateMakeObsolete();
        this.obsoleteFlag = true;
        getComPortFactory().update(this);
    }

    private void validateMakeObsolete() {
        if (this.isObsolete()) {
            throw new TranslatableApplicationException(
                    "comPortIsAlreadyObsolete",
                    "The ComPort with id {0} is already obsolete since {1,date,yyyy-MM-dd HH:mm:ss}",
                    this.getId(),
                    this.getObsoleteDate());
        }
    }

    public String getType() {
        return ComPort.class.getName();
    }

    public Date getModificationDate() {
        return modificationDate;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public boolean isInbound() {
        return false;
    }

    @Override
    public ComServer getComServer() {
        return comServer.get();
    }

    @Override
    public boolean isObsolete() {
        return this.obsoleteFlag;
    }

    @Override
    public Date getObsoleteDate() {
        return obsoleteDate;
    }

    @Override
    public ComPortType getComPortType() {
        return type;
    }

    public void setModificationDate(Date modificationDate) {
        this.modificationDate = modificationDate;
    }

    public void setComServer(ComServer comServer) {
        // Should use ValueReference.of(comServer) ?
        this.comServer.set(comServer);
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isObsoleteFlag() {
        return obsoleteFlag;
    }

    public void setObsoleteFlag(boolean obsoleteFlag) {
        this.obsoleteFlag = obsoleteFlag;
    }

    public void setObsoleteDate(Date obsoleteDate) {
        this.obsoleteDate = obsoleteDate;
    }

    public void setComPortType(ComPortType type) {
        this.type = type;
    }

    public long getId() {
        return id;
    }

    public List<ComPortPool> getComPortPools() {
        List<ComPortPool> comPortPools = new ArrayList<>();
        for (ComPortPoolMember comPortPoolMember : comPortPoolMembers) {
            comPortPools.add(comPortPoolMember.getComPortPool());
        }
        return ImmutableList.copyOf(comPortPools);
    }

    public void setComPortPools(List<ComPortPool> comPortPools) {
        comPortPoolMembers.clear();
        for (ComPortPool comPortPool : comPortPools) {
            comPortPoolMembers.add(new ComPortPoolMemberImpl(comPortPool, this));
        }
    }

    private DataMapper<ComPort> getComPortFactory() {
        return Bus.getServiceLocator().getOrmClient().getComPortFactory();
    }

    @Override
    public void persist() {
        validate();
        if (this.getId()==0) {
            Bus.getServiceLocator().getOrmClient().getComPortFactory().persist(this);
        } else {
            validateUpdateAllowed();
            Bus.getServiceLocator().getOrmClient().getComPortFactory().update(this);
        }
    }

}