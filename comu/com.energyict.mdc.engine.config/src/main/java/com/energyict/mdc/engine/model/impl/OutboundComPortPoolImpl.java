package com.energyict.mdc.engine.model.impl;

import com.energyict.mdc.common.BusinessException;
import com.energyict.cbo.InvalidReferenceException;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.comserver.collections.CollectionFormatter;
import com.energyict.cpo.BatchStatement;
import com.energyict.cpo.ResultSetIterator;
import com.energyict.cpo.SqlBuilder;
import com.energyict.mdc.common.Transaction;
import com.energyict.mdc.ManagerFactory;
import com.energyict.mdc.cpo.Discriminator;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.ComPortPool;
import com.energyict.mdc.engine.model.OutboundComPort;
import com.energyict.mdc.engine.model.OutboundComPortPool;
import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.shadow.ports.OutboundComPortPoolShadow;
import com.energyict.mdc.tasks.ConnectionMethodFactory;
import com.energyict.mdc.tasks.ConnectionTask;
import com.energyict.mdc.tasks.ServerConnectionMethod;
import com.energyict.mdc.xml.ComPortPoolCommand;
import com.energyict.mdw.xml.Command;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides an implementation for the {@link com.energyict.mdc.engine.model.OutboundComPortPool} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-26 (10:21)
 */
public class OutboundComPortPoolImpl extends ComPortPoolImpl<OutboundComPortPoolShadow> implements OutboundComPortPool {

    private TimeDuration taskExecutionTimeout;

    protected OutboundComPortPoolImpl (int id) {
        super(id);
    }

    protected OutboundComPortPoolImpl (ResultSet resultSet, ResultSetIterator resultSetIterator) throws SQLException {
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

    public void init (final OutboundComPortPoolShadow shadow) throws SQLException, BusinessException {
        this.execute(new Transaction<Void>() {
            public Void doExecute () throws BusinessException, SQLException {
                doInit(shadow);
                return null;
            }
        });
    }

    private void doInit (OutboundComPortPoolShadow shadow) throws SQLException, BusinessException {
        this.validateNew(shadow);
        this.copyNew(shadow);
        this.postNew();
        this.createComPortPoolMembers(shadow);
        this.created();
    }

    /**
     * Deletes all existing link objects between OutboundComPortPool and OutboundComPort.
     */
    private void deleteComPortPoolMembers () throws SQLException {
        SqlBuilder sqlBuilder = new SqlBuilder();
        sqlBuilder.append("delete from ");
        sqlBuilder.append(ComPortPoolFactoryImpl.MEMBERSHIP_TABLENAME);
        sqlBuilder.appendWhereOrAnd();
        sqlBuilder.append(ComPortPoolFactoryImpl.MEMBERSHIP_COLUMNS[0]);
        sqlBuilder.append(" = ?");
        sqlBuilder.bindInt(this.getId());
        this.executeSql(sqlBuilder);
    }

    /**
     * Create link objects between OutboundComPortPool and OutboundComPort.
     */
    private void createComPortPoolMembers (OutboundComPortPoolShadow shadow) throws SQLException {
        BatchStatement batchStatement = this.getBatchCreateStatement(shadow);
        this.bindBatchCreateStatement(shadow, batchStatement);
        batchStatement.executeBatch();
    }

    private BatchStatement getBatchCreateStatement (OutboundComPortPoolShadow shadow) {
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("insert into ");
        sqlBuilder.append(ComPortPoolFactoryImpl.MEMBERSHIP_TABLENAME);
        sqlBuilder.append(" (");
        sqlBuilder.append(ComPortPoolFactoryImpl.MEMBERSHIP_COLUMNS[0]);
        sqlBuilder.append(", ");
        sqlBuilder.append(ComPortPoolFactoryImpl.MEMBERSHIP_COLUMNS[1]);
        sqlBuilder.append(" ) values (?, ?)");
        return new BatchStatement(sqlBuilder.toString(), shadow.getOutboundComPortIds().size());
    }

    private void bindBatchCreateStatement (OutboundComPortPoolShadow shadow, BatchStatement batchStatement) throws SQLException {
        for (Integer comPortId : shadow.getOutboundComPortIds()) {
            batchStatement.setInt(1, this.getId());
            batchStatement.setInt(2, comPortId);
            batchStatement.addBatch();
        }
    }

    @Override
    protected int bindBody (PreparedStatement preparedStatement, int firstParameterNumber) throws SQLException {
        int parameterNumber = super.bindBody(preparedStatement, firstParameterNumber);
        return this.bindTimeDuration(this.taskExecutionTimeout, preparedStatement, parameterNumber);
    }

    private int bindTimeDuration (TimeDuration timeDuration, PreparedStatement preparedStatement, int firstParameterNumber) throws SQLException {
        int parameterNumber = firstParameterNumber;
        preparedStatement.setInt(parameterNumber++, timeDuration.getCount());
        preparedStatement.setInt(parameterNumber++, timeDuration.getTimeUnitCode());
        return parameterNumber;
    }

    @Override
    protected void doLoad (ResultSetIterator resultSet) throws SQLException {
        super.doLoad(resultSet);
        this.taskExecutionTimeout = new TimeDuration(resultSet.nextInt(), resultSet.nextInt());
    }

    private void validateNew (OutboundComPortPoolShadow shadow) throws BusinessException {
        this.validate(shadow);
    }

    private void validateUpdate (OutboundComPortPoolShadow shadow) throws BusinessException {
        this.validateUpdateAllowed();
        this.validate(shadow);
    }

    protected void validate (OutboundComPortPoolShadow shadow) throws BusinessException {
        super.validate(shadow);
        this.validateNotNull(shadow.getTaskExecutionTimeout(), "outboundComPortPool.taskExecutionTimeout");
        this.validateComPorts(shadow.getOutboundComPortIds(), shadow.getType());
    }

    /**
     * Validates that all referenced {@link com.energyict.mdc.engine.model.ComPort} are effectively {@link com.energyict.mdc.engine.model.OutboundComPort}
     * and that their {@link ComPortType type} corresponds with this pool's type.
     *
     * @param outboundComPortIds The ids of the referenced ComPorts
     * @param comPortType The ComPortType of this OutboundComPortPool
     */
    private void validateComPorts (List<Integer> outboundComPortIds, ComPortType comPortType) throws BusinessException {
        for (Integer comPortId : outboundComPortIds) {
            this.validateComPort(comPortId, comPortType);
        }
    }

    private void validateComPort (int comPortId, ComPortType comPortType) throws BusinessException {
        ComPort comPort = this.getComPortFactory().find(comPortId);
        if (comPort == null) {
            throw InvalidReferenceException.newForIdBusinessObject(comPortId, this.getComPortFactory());
        }
        else {
            this.validateComPort(comPort, comPortType);
        }
    }

    private void validateComPort (ComPort comPort, ComPortType comPortType) throws BusinessException {
        if (!(comPort instanceof OutboundComPort)) {
            throw InvalidReferenceException.newForIdBusinessObjectSubClass(comPort.getId(), this.getComPortFactory(), OutboundComPort.class);
        }
        this.validateComPortForComPortType(comPort, comPortType);

    }

    public OutboundComPortPoolShadow getShadow () {
        return new OutboundComPortPoolShadow(this);
    }

    public void update (final OutboundComPortPoolShadow shadow) throws BusinessException, SQLException {
        this.execute(new Transaction<Void>() {
            public Void doExecute () throws BusinessException, SQLException {
                doUpdate(shadow);
                return null;
            }
        });
    }

    protected void doUpdate (OutboundComPortPoolShadow shadow) throws BusinessException, SQLException {
        this.validateUpdate(shadow);
        this.copyUpdate(shadow);
        this.post();
        this.deleteComPortPoolMembers();
        this.createComPortPoolMembers(shadow);
        this.updated();
    }

    private void copyUpdate (OutboundComPortPoolShadow shadow) {
        this.copy(shadow);
    }

    private ComPortFactory getComPortFactory () {
        return ManagerFactory.getCurrent().getComPortFactory();
    }

    private void copyNew (OutboundComPortPoolShadow shadow) {
        this.copy(shadow);
    }

    protected void copy (OutboundComPortPoolShadow shadow) {
        super.copy(shadow);
        this.taskExecutionTimeout = shadow.getTaskExecutionTimeout();
    }

    @Override
    protected void validateDelete () throws BusinessException, SQLException {
        super.validateDelete();
        this.validateNotUsedByConnectionMethods();
    }

    @Override
    protected void deleteDependents () throws SQLException, BusinessException {
        super.deleteDependents();
        this.deleteComPortPoolMembers();
    }

    @Override
    protected void validateMakeObsolete () throws BusinessException {
        super.validateMakeObsolete();
        this.validateNotUsedByConnectionMethods();
    }

    private void validateNotUsedByConnectionMethods() throws BusinessException {
        List<ServerConnectionMethod> connectionMethods = this.getConnectionMethodFactory().findByPool(this);
        if (!connectionMethods.isEmpty()) {
            List<ConnectionTask> connectionTasks = this.collectConnectionTasks(connectionMethods);
            String names = CollectionFormatter.toSeparatedList(toNames(connectionTasks), ",");
            throw new BusinessException(
                    "outboundComPortPoolXStillInUseByConnectionMethodY",
                    "Outbound ComPortPool '{0}' is still in use by the following connection tasks: {1}",
                    this.getName(),
                    names);
        }
    }

    private List<ConnectionTask> collectConnectionTasks (List<ServerConnectionMethod> connectionMethods) {
        List<ConnectionTask> connectionTasks = new ArrayList<>(connectionMethods.size());
        for (ServerConnectionMethod connectionMethod : connectionMethods) {
            connectionTasks.add(connectionMethod.getConnectionTask());
        }
        return connectionTasks;
    }

    @Override
    protected void makeMembersObsolete () throws SQLException {
        this.deleteComPortPoolMembers();
    }

    private ConnectionMethodFactory getConnectionMethodFactory() {
        return ManagerFactory.getCurrent().getConnectionMethodFactory();
    }

    @Override
    protected Discriminator<ComPortPoolImpl> getDiscriminator () {
        return ComPortPoolFactoryImpl.ComPortPoolDiscriminator.OUTBOUND;
    }

    @Override
    public boolean isInbound () {
        return false;
    }

    @Override
    public TimeDuration getTaskExecutionTimeout () {
        return taskExecutionTimeout;
    }

    @Override
    public List<OutboundComPort> getComPorts () {
        return this.getComPortFactory().findOutboundInPool(this);
    }

}