package com.energyict.mdc.engine.model.impl;

import com.energyict.mdc.common.BusinessException;
import com.energyict.cbo.InvalidReferenceException;
import com.energyict.mdc.common.InvalidValueException;
import com.energyict.comserver.collections.CollectionConverter;
import com.energyict.comserver.collections.CollectionFormatter;
import com.energyict.cpo.ResultSetIterator;
import com.energyict.mdc.common.Transaction;
import com.energyict.mdc.ManagerFactory;
import com.energyict.mdc.cpo.Discriminator;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.ComPortPool;
import com.energyict.mdc.engine.model.InboundComPort;
import com.energyict.mdc.engine.model.InboundComPortPool;
import com.energyict.mdc.engine.model.OutboundComPort;
import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.protocol.InboundDeviceProtocolPluggableClassFactory;
import com.energyict.mdc.protocol.api.PluggableClass;
import com.energyict.mdc.protocol.inbound.InboundDeviceProtocol;
import com.energyict.mdc.shadow.ports.InboundComPortPoolShadow;
import com.energyict.mdc.tasks.InboundConnectionTask;
import com.energyict.mdc.tasks.ServerConnectionTaskFactory;
import com.energyict.mdc.xml.ComPortPoolCommand;
import com.energyict.mdw.xml.Command;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Provides an implementation for the {@link com.energyict.mdc.engine.model.InboundComPortPool} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-26 (10:21)
 */
public class InboundComPortPoolImpl extends ComPortPoolImpl<InboundComPortPoolShadow> implements InboundComPortPool {

    private int discoveryProtocolPluggableClassId;
    private PluggableClass<InboundDeviceProtocol> discoveryProtocolPluggableClass;

    protected InboundComPortPoolImpl(int id) {
        super(id);
    }

    protected InboundComPortPoolImpl(ResultSet resultSet, ResultSetIterator resultSetIterator) throws SQLException {
        super(resultSet, resultSetIterator);
    }

    @Override
    public boolean isExportAllowed () {
        return true;
    }

    @Override
    public Command<ComPortPool> createConstructor () {
        return new ComPortPoolCommand(this);
    }

    @Override
    protected int bindBody(PreparedStatement preparedStatement, int firstParameterNumber) throws SQLException {
        int parameterNumber = super.bindBody(preparedStatement, firstParameterNumber);
        preparedStatement.setInt(parameterNumber++, this.discoveryProtocolPluggableClassId);
        return parameterNumber;
    }

    @Override
    protected void doLoad(ResultSetIterator resultSet) throws SQLException {
        super.doLoad(resultSet);
        this.setDiscoveryProtocolPluggableClassId(resultSet.nextInt());
    }

    private void setDiscoveryProtocolPluggableClassId(int discoveryProtocolPluggableClassId) {
        this.discoveryProtocolPluggableClassId = discoveryProtocolPluggableClassId;
        this.discoveryProtocolPluggableClass = null;
    }

    public void init(final InboundComPortPoolShadow shadow) throws SQLException, BusinessException {
        this.execute(new Transaction<Void>() {
            public Void doExecute () throws BusinessException, SQLException {
                doInit(shadow);
                return null;
            }
        });
    }

    private void doInit(InboundComPortPoolShadow shadow) throws SQLException, BusinessException {
        this.validateNew(shadow);
        this.copyNew(shadow);
        this.postNew();
        this.created();
    }

    private void validateNew(InboundComPortPoolShadow shadow) throws BusinessException {
        this.validate(shadow);
    }

    private void validateUpdate(InboundComPortPoolShadow shadow) throws BusinessException {
        this.validateUpdateAllowed();
        this.validate(shadow);
    }

    protected void validate(InboundComPortPoolShadow shadow) throws BusinessException {
        super.validate(shadow);
        this.validateComPorts(shadow.getInboundComPortIds(), shadow.getType());
        this.validateDiscoveryProtocolPluggableClass(shadow.getDiscoveryProtocolPluggableClassId());
    }

    private void validateDiscoveryProtocolPluggableClass(int discoveryProtocolPluggableClassId) throws InvalidReferenceException, InvalidValueException {
        if (discoveryProtocolPluggableClassId == 0) {
            throw new InvalidValueException("XcannotBeEmpty", "\"{0}\" is a required property", "inboundComPortPool.discoveryProtocol");
        } else {
            PluggableClass pluggableClass = this.findDiscoveryProtocolPluggableClass(discoveryProtocolPluggableClassId);
            if (pluggableClass == null) {
                throw InvalidReferenceException.newForIdBusinessObject(discoveryProtocolPluggableClassId, this.getInboundDeviceProtocolPluggableClassFactory());
            }
        }
    }

    private InboundDeviceProtocolPluggableClassFactory getInboundDeviceProtocolPluggableClassFactory () {
        return ManagerFactory.getCurrent().getInboundDeviceProtocolPluggableClassFactory();
    }

    private PluggableClass<InboundDeviceProtocol> findDiscoveryProtocolPluggableClass(int connectionTypePluggableClassId) {
        return getInboundDeviceProtocolPluggableClassFactory().find(connectionTypePluggableClassId);
    }

    /**
     * Validates that all referenced {@link com.energyict.mdc.engine.model.ComPort} are effectively {@link com.energyict.mdc.engine.model.OutboundComPort}
     * and that their {@link ComPortType type} corresponds with this pool's type.
     *
     * @param inboundComPortIds The ids of the referenced ComPorts
     * @param comPortType       The ComPortType of this OutboundComPortPool
     */
    private void validateComPorts(List<Integer> inboundComPortIds, ComPortType comPortType) throws BusinessException {
        for (Integer comPortId : inboundComPortIds) {
            this.validateComPort(comPortId, comPortType);
        }
    }

    private void validateComPort(int comPortId, ComPortType comPortType) throws BusinessException {
        ComPort comPort = this.getComPortFactory().find(comPortId);
        if (comPort == null) {
            throw InvalidReferenceException.newForIdBusinessObject(comPortId, this.getComPortFactory());
        } else {
            this.validateComPort(comPort, comPortType);
        }
    }

    private void validateComPort(ComPort comPort, ComPortType comPortType) throws BusinessException {
        if (!(comPort instanceof InboundComPort)) {
            throw InvalidReferenceException.newForIdBusinessObjectSubClass(comPort.getId(), this.getComPortFactory(), OutboundComPort.class);
        }
        this.validateComPortForComPortType(comPort, comPortType);

    }

    @Override
    public InboundComPortPoolShadow getShadow() {
        return new InboundComPortPoolShadow(this);
    }

    public void update(final InboundComPortPoolShadow shadow) throws BusinessException, SQLException {
        this.execute(new Transaction<Void>() {
            public Void doExecute () throws BusinessException, SQLException {
                doUpdate(shadow);
                return null;
            }
        });
    }

    protected void doUpdate(InboundComPortPoolShadow shadow) throws BusinessException, SQLException {
        this.validateUpdate(shadow);
        this.copyUpdate(shadow);
        this.post();
        this.updated();
    }

    private void copyUpdate(InboundComPortPoolShadow shadow) {
        this.copy(shadow);
    }

    private ComPortFactory getComPortFactory() {
        return ManagerFactory.getCurrent().getComPortFactory();
    }

    private void copyNew(InboundComPortPoolShadow shadow) {
        this.copy(shadow);
    }

    protected void copy(InboundComPortPoolShadow shadow) {
        super.copy(shadow);
        this.setDiscoveryProtocolPluggableClassId(shadow.getDiscoveryProtocolPluggableClassId());
    }

    @Override
    protected Discriminator<ComPortPoolImpl> getDiscriminator() {
        return ComPortPoolFactoryImpl.ComPortPoolDiscriminator.INBOUND;
    }

    @Override
    protected void validateDelete() throws SQLException, BusinessException {
        super.validateDelete();
        this.validateNotUsedByComPorts();
        this.validateNotUsedByConnectionTasks();
    }

    @Override
    protected void validateMakeObsolete() throws BusinessException {
        super.validateMakeObsolete();
        this.validateNotUsedByComPorts();
        this.validateNotUsedByConnectionTasks();
    }

    private void validateNotUsedByComPorts() throws BusinessException {
        List<InboundComPort> comPorts = this.getComPorts();
        if (!comPorts.isEmpty()) {
            String names = CollectionFormatter.toSeparatedList(CollectionConverter.toNames(comPorts), ",");
            throw new BusinessException(
                    "inboundComPortPoolXStillInUseByComPortsY",
                    "Inbound ComPortPool '{0}' is still in use by the following inbound comport(s): {1}",
                    this.getName(),
                    names);
        }
    }

    private void validateNotUsedByConnectionTasks () throws BusinessException {
        ServerConnectionTaskFactory connectionTaskFactory = this.getConnectionTaskFactory();
        List<InboundConnectionTask> connectionTasks = connectionTaskFactory.findInboundUsingComPortPool(this);
        if (!connectionTasks.isEmpty()) {
            String names = CollectionFormatter.toSeparatedList(toNames(connectionTasks), ",");
            throw new BusinessException(
                    "inboundComPortPoolXStillInUseByConnectionTasksY",
                    "Inbound ComPortPool '{0}' is still in use by the following inbound connection tasks(s): {1}",
                    this.getName(),
                    names);
        }
    }

    private ServerConnectionTaskFactory getConnectionTaskFactory () {
        return ManagerFactory.getCurrent().getConnectionTaskFactory();
    }

    @Override
    protected void makeMembersObsolete() {
        /* Can only be made obsolete if there are no members
         * so nothing to do here. */
    }

    @Override
    public boolean isInbound() {
        return true;
    }

    @Override
    public List<InboundComPort> getComPorts() {
        return this.getComPortFactory().findInboundInPool(this);
    }

    @Override
    public PluggableClass<InboundDeviceProtocol> getDiscoveryProtocolPluggableClass() {
        if (this.discoveryProtocolPluggableClass == null) {
            this.discoveryProtocolPluggableClass = this.findDiscoveryProtocolPluggableClass(this.discoveryProtocolPluggableClassId);
        }
        return this.discoveryProtocolPluggableClass;
    }

}