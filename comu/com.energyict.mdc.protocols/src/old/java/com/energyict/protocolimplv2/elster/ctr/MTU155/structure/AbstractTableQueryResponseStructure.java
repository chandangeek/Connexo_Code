package com.energyict.protocolimplv2.elster.ctr.MTU155.structure;

import com.energyict.protocolimplv2.elster.ctr.MTU155.frame.field.Data;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.AbstractCTRObject;

import java.util.List;

/**
 * Abstract table class. All subclasses have the method getObjects().
 * Copyrights EnergyICT
 * Date: 2-nov-2010
 * Time: 14:26:00
 */
public abstract class AbstractTableQueryResponseStructure<T extends AbstractTableQueryResponseStructure> extends Data<T> {

    public AbstractTableQueryResponseStructure(boolean longFrame) {
        super(longFrame);
    }

    public abstract List<AbstractCTRObject> getObjects();
}