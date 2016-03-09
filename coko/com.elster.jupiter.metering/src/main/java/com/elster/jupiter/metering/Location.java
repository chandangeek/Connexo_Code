package com.elster.jupiter.metering;

import aQute.bnd.annotation.ProviderType;

import java.util.List;
import java.util.Optional;

@ProviderType
public interface Location {

    long getId();
    String getName();
    List<? extends LocationMember> getMembers();
    Optional<LocationMember> getMember(String locale);
    void remove();

}
