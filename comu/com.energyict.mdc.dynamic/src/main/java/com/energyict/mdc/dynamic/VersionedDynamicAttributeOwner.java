package com.energyict.mdc.dynamic;

import java.time.Instant;

public interface VersionedDynamicAttributeOwner extends DynamicAttributeOwner {

    public Instant getFrom();

    public Instant getTo();

    public void setFrom(Instant date);

    public void setTo(Instant date);

}