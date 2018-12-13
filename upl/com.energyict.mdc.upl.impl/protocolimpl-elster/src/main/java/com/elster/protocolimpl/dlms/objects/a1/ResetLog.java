/* File:        
 * $HeadURL: $
 * Version:     
 * $Id: $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  heuckeg
 * Created on:  24.04.2014 13:52:23
 */

package com.elster.protocolimpl.dlms.objects.a1;

import com.elster.dlms.cosem.applicationlayer.CosemApplicationLayer;
import com.elster.dlms.cosem.classes.common.CosemClassIds;
import com.elster.dlms.types.basic.CosemMethodDescriptor;
import com.elster.dlms.types.basic.ObisCode;
import com.elster.dlms.types.data.DlmsData;
import com.elster.dlms.types.data.DlmsDataInteger;

import java.io.IOException;

/**
 * This class ...
 *
 * @author heuckeg
 */
class ResetLog implements IReadWriteObject
{
    private final ObisCode obisCode;

    public ResetLog(ObisCode obisCode)
    {
        this.obisCode = obisCode;
    }

    public ObisCode getObisCode()
    {
        return obisCode;
    }

    public void write(CosemApplicationLayer layer, Object[] data) throws IOException
    {
        DlmsData parameter = new DlmsDataInteger(0);
        CosemMethodDescriptor method = new CosemMethodDescriptor(obisCode, CosemClassIds.PROFILE_GENERIC, 1);
        layer.executeActionAndCheckResponse(method, parameter);
    }

    public Object read(CosemApplicationLayer layer) throws IOException
    {
        return null;
    }

}
