package com.energyict.mdc.engine.model.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.Checks;
import com.elster.jupiter.util.time.UtcInstant;
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

import com.google.inject.Provider;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.inject.Inject;

/**
 * Serves as the root of class hierarchy that will provide
 * an implementation for the {@link com.energyict.mdc.engine.model.ComPort} interface hierarchy.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-02 (12:48)
 */
public abstract class ComPortImpl implements ServerComPort {

    protected static final String MODEM_DISCRIMINATOR = "0";
    protected static final String TCP_DISCRIMINATOR = "1";
    protected static final String SERVLET_DISCRIMINATOR = "2";
    protected static final String UDP_DISCRIMINATOR = "3";
    protected static final String OUTBOUND_DISCRIMINATOR = "5";

    static final Map<String, Class<? extends ComPort>> IMPLEMENTERS =
            ImmutableMap.<String, Class<? extends ComPort>>of(
                    MODEM_DISCRIMINATOR, ModemBasedInboundComPortImpl.class,
                    TCP_DISCRIMINATOR, TCPBasedInboundComPortImpl.class,
                    SERVLET_DISCRIMINATOR, ServletBasedInboundComPortImpl.class,
                    UDP_DISCRIMINATOR, UDPBasedInboundComPortImpl.class,
                    OUTBOUND_DISCRIMINATOR, OutboundComPortImpl.class);
    private final DataModel dataModel;
    private final Provider<ComPortPoolMember> comPortPoolMemberProvider;

    private long id=0;
    private String name;
    private UtcInstant modificationDate;
    private final Reference<ComServer> comServer = ValueReference.absent();
    private boolean active;
    private String description;
    private boolean obsoleteFlag;
    private UtcInstant obsoleteDate;
    private ComPortType type;
    private final List<ComPortPoolMember> comPortPoolMembers = new ArrayList<>();

    /**
     * Constructor for Kore persistence
     * @param dataModel
     */
    @Inject
    protected ComPortImpl(DataModel dataModel, Provider<ComPortPoolMember> comPortPoolMemberProvider) {
        this.dataModel = dataModel;
        this.comPortPoolMemberProvider = comPortPoolMemberProvider;
    }

    public void setName(String name) {
        this.validateName(name);
        this.name = name;
    }

    protected void validate() {
        validateNotNull(this.name, "name");
        validateNotNull(this.type, "type");
        validateName(this.name);
    }

    private void validateUpdateAllowed() {
        if (this.obsoleteFlag) {
            throw new TranslatableApplicationException("comport.noUpdateAllowed", "This comport is marked as deleted, no updates allowed.");
        }
    }

    private void validateName(String nameToValidate)  {
        for (ComPort comPort : comServer.get().getComPorts()) {
            if (comPort.getId()!=this.getId() && comPort.getName().equals(nameToValidate)) {
                throw new TranslatableApplicationException("duplicateComPortX", "A ComPort by the name of \"{0}\" already exists", nameToValidate);
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

    public UtcInstant getModificationDate() {
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
    public UtcInstant getObsoleteDate() {
        return obsoleteDate;
    }

    @Override
    public ComPortType getComPortType() {
        return type;
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
            ComPortPoolMember comPortPoolMember = comPortPoolMemberProvider.get();
            comPortPoolMember.setComPort(this);
            comPortPoolMember.setComPortPool(comPortPool);
            comPortPoolMembers.add(comPortPoolMember);
        }
    }

    @Override
    public void init(ComServer owner) {
        this.setComServer(owner);
    }

    @Override
    public void save() {
        validate();
        if (this.getId()==0) {
            throw new IllegalStateException("ComPort should have been created using the ComServer, how did you end up here?");
        } else {
            validateUpdateAllowed();
            dataModel.update(this);
        }
    }

    @Override
    public void makeObsolete(){
        this.validateMakeObsolete();
        this.obsoleteFlag = true;
        removeFromComPortPools();
        dataModel.update(this);
    }

    private void removeFromComPortPools() {
        Iterator<ComPortPoolMember> iterator = comPortPoolMembers.iterator();
        while(iterator.hasNext()) {
            ComPortPoolMember comPortPoolMember = iterator.next();
            if (comPortPoolMember.getComPort().getId()==this.id) {
                iterator.remove();
            }
        }
    }

    static protected class ComPortBuilderImpl<B extends ComPort.Builder<B, C>, C extends ComPort> implements ComPort.Builder<B, C> {
        C comPort;
        B self;

        protected ComPortBuilderImpl(Class<B> clazz, C comPort) {
            this.comPort = comPort;
            self = clazz.cast(this);
        }

        @Override
        public B comPortType(ComPortType comPortType) {
            comPort.setComPortType(comPortType);
            return self;
        }

        @Override
        public B name(String name) {
            comPort.setName(name);
            return self;
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
            ((ComPortImpl)comPort).validate();
            return comPort;
        }
    }
}