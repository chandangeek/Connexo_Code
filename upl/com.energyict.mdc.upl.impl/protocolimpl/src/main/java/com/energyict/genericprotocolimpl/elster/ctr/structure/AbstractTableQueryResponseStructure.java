package com.energyict.genericprotocolimpl.elster.ctr.structure;

import com.energyict.genericprotocolimpl.elster.ctr.frame.field.Data;
import com.energyict.genericprotocolimpl.elster.ctr.object.AbstractCTRObject;

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