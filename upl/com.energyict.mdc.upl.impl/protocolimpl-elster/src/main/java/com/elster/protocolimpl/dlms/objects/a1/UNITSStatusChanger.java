/* File:        
 * $HeadURL: $
 * Version:     
 * $Id: $
 * Copyright:   Elster GmbH 2009
 *
 * Created by:  heuckeg
 * Created on:  31.03.2014 09:01:17
 */
package com.elster.protocolimpl.dlms.objects.a1;

import com.elster.dlms.cosem.applicationlayer.CosemApplicationLayer;
import com.elster.dlms.cosem.classes.common.CosemClassIds;
import com.elster.dlms.types.basic.CosemAttributeDescriptor;
import com.elster.dlms.types.basic.CosemMethodDescriptor;
import com.elster.dlms.types.basic.ObisCode;
import com.elster.dlms.types.data.DlmsData;
import com.elster.dlms.types.data.DlmsDataLongUnsigned;

import java.io.IOException;

/**
 * This class ...
 *
 * @author heuckeg
 */
public class UNITSStatusChanger implements IReadWriteObject
{
    private static final ObisCode A1_DEVICE_COND = new ObisCode(7, 0, 96, 5, 0, 255);
    private static final ObisCode A1_CHANGE_DEVICE_COND_SCRIPT = new ObisCode(0, 0, 10, 0, 0, 255);
    //
    private static final int SCR_NORMAL_FROM_UNCONF = 1;
    private static final int SCR_NORMAL_FROM_MAINT = 3;
    private static final int SCR_MAINT_FROM_NORMAL = 2;
    private static final int SCR_UNCONF = 128;

    //
    public enum DeviceState
    {
        NORMAL, MAINTENANCE, UNCONFIGURED, UNKNOWN
    }

    //
    private DeviceState currState = DeviceState.UNKNOWN;

    public UNITSStatusChanger()
    {
    }

    public ObisCode getObisCode()
    {
        return A1_DEVICE_COND;
    }

    public void write(CosemApplicationLayer layer, Object[] data) throws IOException
    {
        if ((data == null) || (data.length == 0) || !(data[0] instanceof DeviceState))
        {
            throw new IllegalArgumentException();
        }

        DeviceState newState = (DeviceState)data[0];
        if (getCurrentState(layer, false) == newState)
            return;

        DlmsData parameter = getParameters(layer, newState);
        CosemMethodDescriptor method = new CosemMethodDescriptor(A1_CHANGE_DEVICE_COND_SCRIPT, 9, 1);

        layer.executeActionAndCheckResponse(method, parameter);

        getCurrentState(layer, true);
    }

    public Object read(CosemApplicationLayer layer) throws IOException
    {
        final CosemAttributeDescriptor value
                = new CosemAttributeDescriptor(A1_DEVICE_COND, CosemClassIds.DATA, 2);

        return layer.getAttributeAndCheckResult(value);
    }

    public DeviceState getCurrentState(CosemApplicationLayer layer, boolean refresh) throws IOException
    {
        if ((currState == DeviceState.UNKNOWN) || refresh)
        {
            DlmsData data = (DlmsData)read(layer);
            if (data == null)
            {
                return DeviceState.UNKNOWN;
            }
            if (!(data.getValue() instanceof Number))
            {
                return DeviceState.UNKNOWN;
            }

            int state = ((Number)data.getValue()).intValue();
            switch (state)
            {
                case 1:  //normal
                    currState = DeviceState.NORMAL;
                    break;
                case 2:  //maint
                    currState = DeviceState.MAINTENANCE;
                    break;
                case 3:  //unconf.
                    currState = DeviceState.UNCONFIGURED;
                    break;
                default:
                    currState = DeviceState.UNKNOWN;
            }
        }
        return currState;
    }

    public DlmsData getParameters(CosemApplicationLayer layer, DeviceState destState) throws IOException
    {
        switch (destState)
        {
            case MAINTENANCE:
                return new DlmsDataLongUnsigned(SCR_MAINT_FROM_NORMAL);
            case NORMAL:
            {
                if (getCurrentState(layer, false) == DeviceState.MAINTENANCE)
                {
                    return new DlmsDataLongUnsigned(SCR_NORMAL_FROM_MAINT);
                }
                else
                {
                    return new DlmsDataLongUnsigned(SCR_NORMAL_FROM_UNCONF);
                }
            }
            case UNCONFIGURED:
                return new DlmsDataLongUnsigned(SCR_UNCONF);
            default:
                throw new IllegalStateException("Unexpected dest. state: " + destState);
        }
    }

}
