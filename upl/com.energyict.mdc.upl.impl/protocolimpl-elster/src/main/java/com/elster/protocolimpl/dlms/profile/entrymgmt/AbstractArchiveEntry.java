package com.elster.protocolimpl.dlms.profile.entrymgmt;

import com.elster.dlms.types.basic.ObisCode;

/**
 * User: heuckeg
 * Date: 15.04.13
 * Time: 11:17
 */
public abstract class AbstractArchiveEntry
{
    // Initialized by constructor
    private final ObisCode obisCode;
    private final int attribute;
    //
    private int index;

    public AbstractArchiveEntry(ObisCode obisCode, final int attribute)
    {
        this.obisCode = obisCode;
        this.attribute = attribute;
        index = -1;
    }

    public ObisCode getObisCode()
    {
        return obisCode;
    }

    public int getAttribute()
    {
        return attribute;
    }

    @Override
    public String toString()
    {
        return obisCode.toString() + "A" + attribute;
    }

    public int getIndex()
    {
        return index;
    }

    public void setIndex(int index)
    {
        this.index = index;
    }
}
