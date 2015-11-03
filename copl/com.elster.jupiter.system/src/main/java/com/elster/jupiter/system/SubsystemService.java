package com.elster.jupiter.system;

import aQute.bnd.annotation.ProviderType;
import java.util.List;

@ProviderType
public interface SubsystemService {

    String COMPONENTNAME = "SBS";
    
    List<Subsystem> getSubsystems();
    void registerSubsystem(Subsystem subsystem);

}
