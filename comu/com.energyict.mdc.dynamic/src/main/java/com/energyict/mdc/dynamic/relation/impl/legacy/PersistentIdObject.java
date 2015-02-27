package com.energyict.mdc.dynamic.relation.impl.legacy;

import com.energyict.mdc.common.ApplicationException;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.BusinessObject;
import com.energyict.mdc.common.DatabaseException;
import com.energyict.mdc.common.IdBusinessObject;
import com.energyict.mdc.common.SqlBuilder;

import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.util.Checks;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

/**
 * Replaces the PersistentIdObject class from the mdw persistence framework
 * wiring most of the methods to the new Jupiter-Kore persistence framework.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-12-17 (11:59)
 */
public abstract class PersistentIdObject implements IdBusinessObject {

    private long id;
    private final DataModel dataModel;

    protected PersistentIdObject(DataModel dataModel) {
        super();
        this.dataModel = dataModel;
    }

    public int getId() {
        return (int) id;
    }

    protected DataModel getDataModel() {
        return dataModel;
    }

    @Override
    public BusinessObject getBusinessObject() {
        return this;
    }

    protected abstract DataMapper getDataMapper();

    /**
     * Saves this object for the first time.
     */
    protected void postNew() {
        this.getDataMapper().persist(this);
    }

    /**
     * Updates the changes made to this object.
     */
    protected void post() {
        this.getDataMapper().update(this);
    }

    public void delete() throws BusinessException, SQLException {
        this.validateDelete();
        this.deleteDependents();
        this.getDataMapper().remove(this);
    }

    /**
     * Tests if the receiver can be deleted.
     *
     * @throws BusinessException Thrown when a business constraint was violated
     * @throws SQLException Thrown when a database constraint was violated
     */
    protected void validateDelete() throws SQLException, BusinessException {
    }

    /**
     * Deletes all dependent objects.
     *
     * @throws BusinessException Thrown when a business constraint was violated
     * @throws SQLException Thrown when a database constraint was violated
     */
    protected void deleteDependents() throws SQLException, BusinessException {
    }

    /**
     * Two PersistentIdObjects are considered equal if they
     * represent the same tuple
     *
     * @param other object to test equality with
     * @return true if equal false otherwise
     */
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (this.getClass() == other.getClass()) {
            return this.getId() == ((PersistentIdObject) other).getId();
        }
        else {
            return false;
        }
    }

    public int hashCode() {
        return Objects.hash(id);
    }

    public Serializable getPrimaryKey() {
        return new Long(getId());
    }

    public byte[] getHandle() {
        // Not applicable to subclasses known to this bundle
        return null;
    }

    public final boolean proxies(BusinessObject obj) {
        return this.equals(obj);
    }

    protected void validateMaxLength(String value, String fieldName, int maxlen, boolean isNullable) throws BusinessException {
        this.validateMaxLength(value, fieldName, null, maxlen, isNullable);
    }

    protected void validateMaxLength(String value, String fieldName, String objectName, int maxlen, boolean isNullable) throws BusinessException {
        String valueToValidate = value;
        if (!isNullable) {
            validateNull(valueToValidate, fieldName, objectName);
        }
        else {
            if (valueToValidate == null) {
                valueToValidate = "";
            }
        }
        if (valueToValidate.length() > maxlen) {
            throw new BusinessException(
                        "fieldXMaxYLong",
                        "The field \"{0}\" can only be {1} characters long",
                        this.getTranslation(fieldName, objectName),
                        String.valueOf(maxlen));
        }
    }

    protected void validateNull(String value, String fieldName, String objectName) throws BusinessException {
        if (Checks.is(value).emptyOrOnlyWhiteSpace()) {
            throw new BusinessException(
                    "fieldXCannotBeEmpty",
                    "The field \"{0}\" cannot be empty",
                    this.getTranslation(fieldName, objectName)
            );
        }
    }

    private String getTranslation(String fieldName, String objectName) {
        if (fieldName == null) {
            return "";
        }
        else {
            return objectName + " > " + fieldName;
        }
    }

    public Connection getConnection() {
        return getConnection(this.getDataModel());
    }

    protected static Connection getConnection(DataModel dataModel) {
        try {
            return dataModel.getConnection(true);
        }
        catch (SQLException e) {
            throw new UnderlyingSQLFailedException(e);
        }
    }

    public static void lock(PersistentIdObject persistentObject, String tableName) {
        try {
            try (PreparedStatement stmnt = getLockSqlBuilder(tableName, persistentObject.getId()).getStatement(getConnection(persistentObject.getDataModel()))) {
                try (ResultSet rs = stmnt.executeQuery()) {
                    if (rs.next()) {
                        return;
                    }
                    else {
                        throw new ApplicationException("Tuple not found");
                    }
                }
            }
        } catch (SQLException ex) {
            throw new DatabaseException(ex);
        }
    }

    private static SqlBuilder getLockSqlBuilder(String tableName, int id) {
        SqlBuilder sqlBuilder = new SqlBuilder("select *");
        sqlBuilder.append(" from ");
        sqlBuilder.append(tableName);
        sqlBuilder.append(" where id = ?");
        sqlBuilder.bindInt(id);
        sqlBuilder.append(" for update");
        return sqlBuilder;
    }

}