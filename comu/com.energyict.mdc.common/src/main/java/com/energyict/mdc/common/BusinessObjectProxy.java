package com.energyict.mdc.common;

public interface BusinessObjectProxy {

    public BusinessObject getBusinessObject();

    public String getType();

    public String displayString();

    public boolean proxies(BusinessObject obj);
}
