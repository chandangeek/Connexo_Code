package com.energyict.protocolimpl.elster.ctr.object;

import com.energyict.protocolimpl.elster.ctr.object.attribute.*;

/**
 * Copyrights EnergyICT
 * Date: 6-aug-2010
 * Time: 15:35:22
 */
public interface CTRObject {

    ObjectIdentifier getIdentifier();

    ObjectQualifier getQualifier();

    ObjectValue getValue();

    ObjectAccessDescriptor getAccessDescriptor();

    ObjectDefaultValue getDefaultValue();

}
