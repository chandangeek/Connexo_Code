package com.elster.protocolimpl.dlms.profile.entrymgmt;

import com.elster.dlms.cosem.classes.class07.CaptureObjectDefinition;
import com.elster.dlms.types.basic.ObisCode;

/**
 * User: heuckeg
 * Date: 17.04.13
 * Time: 08:38
 */
@SuppressWarnings({"unused"})
public class CapturedObjects
{
    private final CaptureObjectDefinition[] archiveObjects;

    public CapturedObjects(CaptureObjectDefinition[] archiveObjects)
    {
        this.archiveObjects = archiveObjects;
    }

    public CaptureObjectDefinition[] getObjects()
    {
        return archiveObjects;
    }

    public CaptureObjectDefinition get(final int index)
    {
        return archiveObjects[index];
    }

    public int indexOf(ObisCode obisCode, int attributeIndex)
    {
        for (int i = 0; i < archiveObjects.length; i++)
        {
            CaptureObjectDefinition object = archiveObjects[i];
            if ((object.getLogicalName().equals(obisCode)) &&
                    (object.getAttributeIndex() == attributeIndex))
            {
                return i;
            }
        }
        return -1;
    }

    public CaptureObjectDefinition find(ObisCode obisCode, int attributeIndex)
    {
        for (CaptureObjectDefinition object: archiveObjects)
        {
            if ((object.getLogicalName().equals(obisCode)) &&
                    (object.getAttributeIndex() == attributeIndex))
            {
                return object;
            }
        }
        return null;
    }
}
