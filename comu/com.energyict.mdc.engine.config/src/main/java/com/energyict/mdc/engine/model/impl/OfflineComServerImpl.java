package com.energyict.mdc.engine.model.impl;

import com.energyict.mdc.common.BusinessException;
import com.energyict.cpo.ResultSetIterator;
import com.energyict.mdc.common.Transaction;
import com.energyict.mdc.engine.model.OfflineComServer;
import com.energyict.mdc.ports.OutboundComPort;
import com.energyict.mdc.shadow.servers.OfflineComServerShadow;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

/**
 * Provides an implementation for the {@link com.energyict.mdc.engine.model.OfflineComServer} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-03-28 (15:37)
 */
public class OfflineComServerImpl extends ComServerImpl<OfflineComServerShadow> implements ServerOfflineComServer {

    protected OfflineComServerImpl () {
        super();
    }

    protected OfflineComServerImpl (int id) {
        super(id);
    }

    protected OfflineComServerImpl (ResultSet resultSet, ResultSetIterator resultSetIterator) throws SQLException {
        super(resultSet, resultSetIterator);
    }

    @Override
    protected ComServerFactoryImpl.ComServerDiscriminator getDiscriminator () {
        return ComServerFactoryImpl.ComServerDiscriminator.OFFLINE;
    }

    @Override
    public String getType () {
        return OfflineComServer.class.getName();
    }

    @Override
    public OfflineComServerShadow getShadow () {
        return new OfflineComServerShadow(this);
    }

    @Override
    protected int bindBody (PreparedStatement preparedStatement, int firstParameterNumber) throws SQLException {
        int parameterNumber = super.bindBody(preparedStatement, firstParameterNumber);
        preparedStatement.setNull(parameterNumber++, Types.VARCHAR);    // event registration URL which is not supported by OfflineComServer
        preparedStatement.setNull(parameterNumber++, Types.INTEGER);    // event registration URL which is not supported by OfflineComServer
        return parameterNumber;
    }

    public void init (final OfflineComServerShadow shadow) throws SQLException, BusinessException {
        this.execute(new Transaction<Void>() {
            public Void doExecute () throws BusinessException, SQLException {
                doInit(shadow);
                return null;
            }
        });
    }

    protected void doInit (OfflineComServerShadow shadow) throws SQLException, BusinessException {
        this.validateNew(shadow);
        this.copyNew(shadow);
        this.postNew();
        this.processOutboundComPorts(shadow);
        this.created();
    }

    private void validateNew (OfflineComServerShadow shadow) throws BusinessException {
        this.validate(shadow);
    }

    private void validateUpdate (OfflineComServerShadow shadow) throws BusinessException {
        this.validateUpdateAllowed();
        this.validate(shadow);
    }

    private void copyNew (OfflineComServerShadow shadow) {
        this.copy(shadow);
    }

    @Override
    public void update (final OfflineComServerShadow shadow) throws BusinessException, SQLException {
        this.execute(new Transaction<Void>() {
            public Void doExecute () throws BusinessException, SQLException {
                doUpdate(shadow);
                return null;
            }
        });
    }

    protected void doUpdate(OfflineComServerShadow shadow) throws BusinessException, SQLException {
        this.validateUpdate(shadow);
        this.copyUpdate(shadow);
        this.processOutboundComPorts(shadow);
        this.post();
        this.updated();
    }

    private void copyUpdate (OfflineComServerShadow shadow) {
        this.copy(shadow);
    }

    @Override
    public boolean isOffline () {
        return true;
    }

    @Override
    public List<OutboundComPort> getOutboundComPorts () {
        return super.getOutboundComPorts();
    }

}