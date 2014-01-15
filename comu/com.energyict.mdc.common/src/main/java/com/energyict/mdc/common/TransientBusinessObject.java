package com.energyict.mdc.common;

import java.sql.SQLException;

public abstract class TransientBusinessObject implements BusinessObject {

    public void delete() throws BusinessException, SQLException {
        throw new BusinessException(new UnsupportedOperationException("TransientBusinessObject cannot be deleted"));
    }

    public BusinessObjectFactory getFactory() {
        throw new ApplicationException("This object has no associated factory");
    }

    public String displayString() {
        return toString();
    }

    public BusinessObject getBusinessObject() {
        return this;
    }

    public String getType() {
        Class interfaces[] = this.getClass().getInterfaces();
        if (interfaces.length > 0) {
            return interfaces[0].getName();
        } else {
            return this.getClass().getName();
        }
    }

    public final boolean proxies(BusinessObject obj) {
        return this.equals(obj);
    }

    public final boolean canDelete() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public final TypeId getTypeId() {
        throw new UnsupportedOperationException("getTypeId() is not applicable to [" + this.getClass().getName() + "]");
    }

}