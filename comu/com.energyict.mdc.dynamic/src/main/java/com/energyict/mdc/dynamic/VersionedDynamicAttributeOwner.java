package com.energyict.mdc.dynamic;

import aQute.bnd.annotation.ProviderType;

import java.time.Instant;

@ProviderType
public interface VersionedDynamicAttributeOwner extends DynamicAttributeOwner {

    public Instant getFrom();

    public Instant getTo();

    public void setFrom(Instant date);

    public void setTo(Instant date);

}