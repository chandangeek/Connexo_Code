package com.energyict.mdc.engine.model.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.common.TranslatableApplicationException;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.protocol.api.ComPortType;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

/**
 * Serves as the root of class hierarchy that will provide
 * an implementation for the {@link com.energyict.mdc.engine.model.ComPort} interface hierarchy.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-02 (12:48)
 */
@UniqueName(groups = { Save.Create.class, Save.Update.class }, message = "{"+Constants.MDC_DUPLICATE_COM_PORT+"}")
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
    private final DataModel dataModel;

    private long id=0;
    @NotNull(groups = { Save.Create.class, Save.Update.class }, message = "{"+Constants.MDC_CAN_NOT_BE_EMPTY+"}")
    private String name;
    private Date modificationDate;
    private final Reference<ComServer> comServer = ValueReference.absent();
    private boolean active;
    private String description;
    @Null(groups = { Save.Update.class, Obsolete.class }, message = "{"+Constants.MDC_COMPORT_NO_UPDATE_ALLOWED+"}")
    private Date obsoleteDate;
    @NotNull(groups = { Save.Create.class, Save.Update.class }, message = "{"+Constants.MDC_CAN_NOT_BE_EMPTY+"}")
    private ComPortType type;

    /**
     * Constructor for Kore persistence
     * @param dataModel
     */
    @Inject
    protected ComPortImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    public void setName(String name) {
        this.name = name;
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

    protected void validateInRange(Range<Integer> acceptableRange, int propertyValue, String propertyName) {
        if (!acceptableRange.contains(propertyValue)) {
            throw new TranslatableApplicationException("XnotInAcceptableRange", "\"{0}\" should be between {1} and {2}", propertyName, acceptableRange.lowerEndpoint(), acceptableRange.upperEndpoint());
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
        return this.obsoleteDate!=null;
    }

    @Override
    public Date getObsoleteDate() {
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

    @Override
    public void save() {
        if (this.getId()==0) {
            throw new IllegalStateException("ComPort should have been created using the ComServer, how did you end up here?");
        } else {
            Save.UPDATE.save(dataModel, this);
        }
    }

    @Override
    public void makeObsolete(){
        this.validateMakeObsolete();
        this.obsoleteDate = new Date();
        dataModel.update(this);
    }

    final protected void validateMakeObsolete() {
        Set<ConstraintViolation<ComPortImpl>> constraintViolations = dataModel.getValidatorFactory().getValidator().validate(this, Obsolete.class);
        if (!constraintViolations.isEmpty()) {
            throw new ConstraintViolationException(constraintViolations);
        }
    }

    protected void copyFrom(ComPort source) {
        this.setName(source.getName());
        this.setActive(source.isActive());
        this.setDescription(source.getDescription());
    }

    interface Delete {}

    interface Obsolete {}

    static protected class ComPortBuilderImpl<B extends ComPort.Builder<B, C>, C extends ComPort> implements ComPort.Builder<B, C> {
        C comPort;
        B self;

        protected ComPortBuilderImpl(Class<B> clazz, C comPort) {
            this.comPort = comPort;
            self = clazz.cast(this);
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
            ((ComPortImpl)comPort).validateCreate();
            return comPort;
        }


    }
}