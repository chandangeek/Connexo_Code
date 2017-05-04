/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.ctr.MTU155.structure;

import com.energyict.protocolimplv2.elster.ctr.MTU155.frame.field.Data;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.AbstractCTRObject;

import java.util.List;

public abstract class AbstractTableQueryResponseStructure<T extends AbstractTableQueryResponseStructure> extends Data<T> {

    public AbstractTableQueryResponseStructure(boolean longFrame) {
        super(longFrame);
    }

    public abstract List<AbstractCTRObject> getObjects();
}