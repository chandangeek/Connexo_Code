package com.energyict.mdc.common;

import java.util.List;

/**
 * Provides factory services for {@link IdBusinessObject}s.
 *
 * @author Joost
 */
public interface IdBusinessObjectFactory<S extends IdBusinessObject> extends BusinessObjectFactory<S> {

    public String getTableName();

    /**
     * gets the IdBusinessObject corr. with the given id
     *
     * @param id the id
     * @return the IdBusinessObject corr. with the given id
     */
    public S get(int id);

    /**
     * returns the IdBusinessObjectProxy object for the given IdBusinessObject
     *
     * @param object the IdBusinessObject we want the proxy of
     * @return the IdBusinessObjectProxy object for the given IdBusinessObject
     */
    public BusinessObjectProxy asProxy(IdBusinessObject object);

    /**
     * returns the IdBusinessObjectProxy objects for the given IdBusinessObjects
     *
     * @param businessObjects the IdBusinessObjects we want the proxies of
     * @return the IdBusinessObjectProxy objects for the given IdBusinessObjects
     */
    public List<BusinessObjectProxy> asProxies(List businessObjects);

    /**
     * Returns the meta type factory for this object, eg DeviceType for an Device
     *
     * @return the meta type factory object
     */
    public IdBusinessObjectFactory getMetaTypeFactory();

    /**
     * gets the Class object of the objects this factory creates
     *
     * @return the Class object of the objects this factory creates
     */
    Class<S> getInstanceType();
}
