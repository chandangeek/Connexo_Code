package com.energyict.mdc.dynamic;

import java.util.Date;

public interface VersionedDynamicAttributeOwner extends DynamicAttributeOwner {

    public Date getFrom();

    public Date getTo();

    public void setFrom(Date date);

    public void setTo(Date date);

}