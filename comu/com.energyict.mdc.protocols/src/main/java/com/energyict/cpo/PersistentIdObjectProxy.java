package com.energyict.cpo;

import com.energyict.mdc.common.BusinessObject;
import com.energyict.mdc.common.BusinessObjectProxy;
import com.energyict.mdc.common.IdBusinessObject;
import com.energyict.mdc.common.IdBusinessObjectFactory;

import java.io.Serializable;

public class PersistentIdObjectProxy implements BusinessObjectProxy, Serializable {

    private int id;
    private IdBusinessObjectFactory factory;
    private String typeString;
    private long age;
    private IdBusinessObject bo;
    // Max # milliseconds to keep the BusinessObject cached:
    private static final long MAX_AGE = 5000;

    /**
     * Creates a new instance of PersistentIdObjectProxy
     */
    public PersistentIdObjectProxy(IdBusinessObjectFactory factory, IdBusinessObject object) {
        this.factory = factory;
        this.bo = object;
        this.id = object.getId();
        this.typeString = object.getType();
        age = -1; //System.currentTimeMillis();
    }

    public PersistentIdObjectProxy(IdBusinessObject object) {
        this((IdBusinessObjectFactory) object.getFactory(), object);
    }

    public PersistentIdObjectProxy(IdBusinessObjectFactory factory, int id) {
        this.factory = factory;
        this.id = id;
        this.age = 0;
    }

    public IdBusinessObject getBusinessObject() {
        if (age < 0) {
            age = System.currentTimeMillis();
        } else if (System.currentTimeMillis() - age > MAX_AGE) {
            bo = factory.get(id);
            age = System.currentTimeMillis();
        }
        return bo;
    }

    public String getType() {
        return typeString;
    }

    public int getId() {
        return id;
    }

    public String displayString() {
        BusinessObject b = getBusinessObject();
        if (b == null) {
            return null;
        } else {
            return b.displayString();
        }
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        try {
            PersistentIdObjectProxy proxy = (PersistentIdObjectProxy) obj;
            return this.getType().equals(proxy.getType()) && (this.id == proxy.id);
        } catch (ClassCastException ex) {
            return false;
        }
    }

    public int hashCode() {
        return getType().hashCode() ^ id;
    }

    public void expire() {
        age = 0L;
    }

    // Is this proxy a proxy for the given business object?
    // (To avoid database access just to know this)

    public boolean proxies(BusinessObject obj) {
        return bo == null ? false : bo.equals(obj);
    }

    protected IdBusinessObjectFactory getFactory(){
        return factory;
    }

}