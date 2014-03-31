package com.energyict.mdc.dynamic.impl;

import com.energyict.mdc.common.BusinessObjectFactory;
import com.energyict.mdc.common.BusinessObjectProxy;
import com.energyict.mdc.common.IdBusinessObject;
import com.energyict.mdc.common.IdBusinessObjectFactory;
import com.energyict.mdc.common.PropertiesMetaData;
import com.energyict.mdc.common.TypeId;
import java.io.Serializable;
import java.util.List;

public class TestBusinessObjectFactoryImpl implements TestBusinessObjectFactory {
    @Override
    public String getTableName() {
        return BasicPropertySpecTest.class.getSimpleName();
    }

    @Override
    public TestBusinessObject get(int id) {
        return null;
    }

    @Override
    public BusinessObjectProxy asProxy(IdBusinessObject object) {
        return null;
    }

    @Override
    public List<BusinessObjectProxy> asProxies(List businessObjects) {
        return null;
    }

    @Override
    public IdBusinessObjectFactory getMetaTypeFactory() {
        return null;
    }

    @Override
    public Class<TestBusinessObject> getInstanceType() {
        return TestBusinessObject.class;
    }

    @Override
    public List<TestBusinessObject> findAll() {
        return null;
    }

    @Override
    public Class getShadowClass() {
        return null;
    }

    @Override
    public PropertiesMetaData getPropertiesMetaData() {
        return null;
    }

    @Override
    public String getType() {
        return null;
    }

    @Override
    public TestBusinessObject findByPrimaryKey(Serializable key) {
        return null;
    }

    @Override
    public TestBusinessObject findByHandle(byte[] handle) {
        return null;
    }

    @Override
    public BusinessObjectFactory getSubtypeFactory() {
        return null;
    }

    @Override
    public int getId() {
        return 0;
    }

    @Override
    public TypeId getTargetTypeId() {
        return null;
    }

    @Override
    public boolean isMetaTypeFactory() {
        return false;
    }
}