package com.elster.jupiter.bpm;


import com.elster.jupiter.properties.HasDynamicProperties;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.util.HasName;

import aQute.bnd.annotation.ProviderType;

import javax.ws.rs.Produces;
import java.util.List;
import java.util.Map;

@ProviderType
public interface ProcessAssociationProvider extends HasName, HasDynamicProperties {

    String getType();
}
