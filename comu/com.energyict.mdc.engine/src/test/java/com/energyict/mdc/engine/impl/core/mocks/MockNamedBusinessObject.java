package com.energyict.mdc.engine.impl.core.mocks;

import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.BusinessObject;
import com.energyict.mdc.common.BusinessObjectFactory;
import com.energyict.mdc.common.NamedBusinessObject;
import com.energyict.mdc.common.TypeId;

import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
* Provides a mock implementation for the {@link NamedBusinessObject} interface.
*
* @author Rudi Vankeirsbilck (rudi)
* @since 2012-04-03 (12:03)
*/
public abstract class MockNamedBusinessObject implements NamedBusinessObject, Cloneable {

    private final int id;
    private String name;
    private AtomicBoolean dirty = new AtomicBoolean(false);

    public MockNamedBusinessObject (String name) {
        this(0, name);
    }

    public MockNamedBusinessObject (int id, String name) {
        super();
        this.id = id;
        this.name = name;
    }

    public boolean isDirty () {
        return dirty.get();
    }

    protected void setDirty (boolean dirty) {
        this.dirty.set(dirty);
    }

    protected void becomeDirty () {
        this.setDirty(true);
    }

    protected void becomeClean () {
        this.setDirty(false);
    }

    public String getName () {
        return name;
    }

    public void setName (String name) {
        this.name = name;
    }

    @Override
    public String getExternalName () {
        return null;
    }

    @Override
    public void rename (String name) throws BusinessException, SQLException {
        this.setName(name);
    }

    @Override
    public int getId () {
        return this.id;
    }


    @Override
    public boolean canDelete () {
        return false;
    }

    @Override
    public BusinessObjectFactory getFactory () {
        return null;
    }

    @Override
    public TypeId getTypeId () {
        return null;
    }

    @Override
    public BusinessObject getBusinessObject () {
        return this;
    }

    @Override
    public String getType () {
        return null;
    }

    @Override
    public String displayString () {
        return this.getName();
    }

    @Override
    public boolean proxies (BusinessObject obj) {
        return false;
    }

    @Override
    public Object clone () throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public boolean equals (Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        return this.equals((MockNamedBusinessObject) o);
    }

    private boolean equals (MockNamedBusinessObject that) {
        if (this.id == 0) {
            return this.name.equals(that.name);
        }
        else {
            return this.id == that.id;
        }
    }

    @Override
    public int hashCode () {
        if (this.id == 0) {
            return this.name.hashCode();
        }
        else {
            return id;
        }
    }

}