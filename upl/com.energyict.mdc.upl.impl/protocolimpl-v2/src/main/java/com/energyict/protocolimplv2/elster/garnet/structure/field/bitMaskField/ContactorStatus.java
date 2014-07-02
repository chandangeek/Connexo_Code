package com.energyict.protocolimplv2.elster.garnet.structure.field.bitMaskField;

/**
 * @author sva
 * @since 12/06/2014 - 14:59
 */

import com.energyict.protocolimplv2.elster.garnet.common.field.AbstractBitMaskField;

public abstract class ContactorStatus<T extends AbstractBitMaskField> extends AbstractBitMaskField<T> {

    abstract public int getContactorStatusCode();

    abstract public String getContactorStatusInfo();

}
