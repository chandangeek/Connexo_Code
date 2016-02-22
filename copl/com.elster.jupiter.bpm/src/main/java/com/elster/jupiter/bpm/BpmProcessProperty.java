package com.elster.jupiter.bpm;

import aQute.bnd.annotation.ProviderType;

/**
 * Created by dragos on 2/19/2016.
 */

@ProviderType
public interface BpmProcessProperty {

    String getName();

    Object getValue();

    void setValue(Object value);

    BpmProcessDefinition getProcessDefinition();
}
