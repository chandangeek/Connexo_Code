package com.energyict.mdc.engine.model.impl;

import com.energyict.mdc.common.BusinessException;
import com.energyict.cbo.DuplicateException;
import com.energyict.mdc.common.InvalidValueException;
import com.energyict.cbo.Utils;
import com.energyict.cpo.AuditTrail;
import com.energyict.cpo.AuditTrailFactory;
import com.energyict.mdc.common.Environment;
import com.energyict.cpo.IdObjectShadow;
import com.energyict.cpo.PersistentNamedObject;
import com.energyict.cpo.ResultSetIterator;
import com.energyict.mdc.common.Transaction;
import com.energyict.mdc.ManagerFactory;
import com.energyict.mdc.MdwInterface;
import com.energyict.mdc.common.TranslatableApplicationException;
import com.energyict.mdc.cpo.Discriminator;
import com.energyict.mdc.engine.model.ComPort;
import com.energyict.mdc.engine.model.ComPortPool;
import com.energyict.mdc.journal.ComSession;
import com.energyict.mdc.journal.ComSessionFactory;
import com.energyict.mdc.protocol.api.ComPortType;
import com.energyict.mdc.shadow.ports.ComPortPoolShadow;
import com.energyict.mdc.tasks.ConnectionTask;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Serves as the root for all concrete {@link com.energyict.mdc.engine.model.ComPortPool} interfaces.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-26 (08:47)
 */
public abstract class ComPortPoolImpl implements ComPortPool {

    private boolean active;
    private String description;
    private boolean obsoleteFlag;
    private Date obsoleteDate;
    private ComPortType comPortType;


    protected void validate (ComPortPoolShadow shadow) throws BusinessException {
        this.validateNotNull(shadow.getType(), "comportpool.comporttype");
    }

    protected void validateConstraint (String name) {
        ComPortPool comPortPool = factory.find(name);
        if (comPortPool != null && !comPortPool.isObsolete()) {
            throw new TranslatableApplicationException("duplicateComPortPoolX", "A ComPortPool by the name of \"{0}\" already exists", name);
        }
    }

    protected void validateNotNull (Object propertyValue, String propertyName) throws InvalidValueException {
        if (propertyValue == null) {
            throw new InvalidValueException("XcannotBeEmpty", "\"{0}\" is a required property", propertyName);
        }
    }

    @Override
    protected void deleteDependents() {
        super.deleteDependents();
        this.deleteComSessions();
    }

    private void deleteComSessions() {
        for (ComSession comSession : this.getComSessionFactory().findByPool(this)) {
            comSession.delete();
        }
    }

    @Override
    public void makeObsolete() {
        this.validateMakeObsolete();
        this.makeMembersObsolete();
        this.obsoleteFlag = true;
        this.post();
        this.updateAuditInfo(AuditTrail.ACTION_DELETE);
        this.deleted();
    }

    protected abstract void makeMembersObsolete () throws BusinessException, SQLException;

    protected void validateMakeObsolete () throws BusinessException {
        if (this.isObsolete()) {
            throw new BusinessException(
                    "comPortPoolIsAlreadyObsolete",
                    "The ComPortPool with id {0} is already obsolete since {1,date,yyyy-MM-dd HH:mm:ss}",
                    this.getId(),
                    this.getObsoleteDate());
        }
    }

    protected ComPortPoolFactory factoryInstance () {
        return ManagerFactory.getCurrent().getComPortPoolFactory();
    }

    @Override
    protected void doUpdateAuditInfo (char action) throws SQLException, BusinessException {
        this.getAuditTrailFactory().create(this.getShadow(), ComPortPoolFactoryImpl.ID, action);
    }

    private AuditTrailFactory getAuditTrailFactory () {
        return this.getMdwInterface().getAuditTrailFactory();
    }

    private MdwInterface getMdwInterface () {
        return ManagerFactory.getCurrent().getMdwInterface();
    }

    protected abstract IdObjectShadow getShadow ();

    @Override
    public String toString () {
        if (this.isObsolete()) {
            String pattern = this.getName() + " " + Environment.DEFAULT.get().getTranslation("deleted.on");
            return Utils.format(pattern, new Object[]{getObsoleteDate()});
        }
        else {
            return this.getName();
        }
    }

    @Override
    protected String[] getColumns () {
        return getDiscriminator().getColumns();
    }

    @Override
    protected String getTableName () {
        return ComPortPoolFactoryImpl.TABLENAME;
    }

    public boolean isActive () {
        return active;
    }

    public String getDescription () {
        return description;
    }

    @Override
    public boolean isObsolete () {
        return obsoleteFlag;
    }

    @Override
    public Date getObsoleteDate () {
        if (this.obsoleteFlag && this.obsoleteDate == null) {
            ComPortPool reloadedPool = this.factoryInstance().find(this.getId());
            if (reloadedPool != null) {
                this.obsoleteDate = reloadedPool.getObsoleteDate();
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

    protected void validateComPortForComPortType(ComPort comPort, ComPortType comPortType) throws BusinessException {
        if (!comPort.getComPortType().equals(comPortType)) {
            Object[] messageArguments = new Object[2];
            messageArguments[0] = comPort.getComPortType();
            messageArguments[1] = comPortType;
            throw new BusinessException("comPortTypeOfComPortXDoesNotMatchWithComPortPoolY", "The ComPortType of ComPort {0} does not match with that of the ComPortPool {1}", messageArguments);
        }
    }

    /**
     * Converts the Collection of {@link com.energyict.mdc.tasks.ConnectionTask} to
     * the Collection of each of the names of the ConnectionTasks.
     * The order of the names is the same as the order in which
     * the ConnectionTasks where produced by the provided Collection
     *
     * @param connectionTasks The Collection of ConnectionTasks
     * @return The Collection of each name of the ConnectionTasks
     */
    protected <E extends ConnectionTask> List<String> toNames(Collection<E> connectionTasks) {
        List<String> names = new ArrayList<>(connectionTasks.size());
        for (ConnectionTask connectionTask : connectionTasks) {
            names.add(connectionTask.getName());
        }
        return names;
    }

    @Override
    public boolean isExportAllowed() {
        return true;
    }
}