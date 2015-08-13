/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elster.protocolimpl.dlms.util;

import com.elster.dlms.cosem.applicationlayer.CosemApplicationLayer;
import com.elster.dlms.cosem.classes.common.CosemClassIds;
import com.elster.dlms.types.basic.CosemAttributeDescriptor;
import com.elster.dlms.types.basic.ObisCode;
import com.elster.dlms.types.data.DlmsData;
import com.elster.dlms.types.data.DlmsDataVisibleString;

import java.io.IOException;

/**
 *
 * @author heuckeg
 */
public class A1Utils
{
    static Integer scaler = null;

    public static Integer calculateScalerFromType(CosemApplicationLayer layer)
    {
      if (scaler == null)
      {
        final CosemAttributeDescriptor value = new CosemAttributeDescriptor(new ObisCode("7.128.0.0.2.255"),
                CosemClassIds.DATA, 2);
        DlmsData data;
        try
        {
            data = layer.getAttributeAndCheckResult(value);
        }
        catch (IOException ex)
        {
            return null;
        }

        String deviceName = ((DlmsDataVisibleString) data).getValue();

        String[] parts = deviceName.split(" ");
        MeterType meterType = MeterType.findByName(parts[0]);
        if (meterType == null)
        {
            return null;
        }
        scaler = meterType.getScaler();
      }
      return scaler;
    }

    private enum MeterType
    {
        BK_G1D6("BK-G1,6", -3),
        BK_G2D5("BK-G2,5", -3),
        BK_G4("BK-G4", -3),
        BK_G6("BK-G6", -3),
        BK_G6T("BK-G6T", -3),
        BK_G10T("BK-G10T", -2),
        BK_G16T("BK-G16T", -2),
        BK_G10("BK-G10", -2),
        BK_G16("BK-G16", -2),
        BK_G25T("BK-G25T", -2),
        BK_G25("BK-G25", -2),
        BK_G40("BK-G40", -2),
        BK_G65("BK-G65", -2),
        BK_G100("BK-G100", -1);

        private final String name;
        private final int scaler;

        private MeterType(final String name, final int scaler)
        {
            this.name = name;
            this.scaler = scaler;
        }

        public String getName()
        {
            return name;
        }

        public int getScaler()
        {
            return scaler;
        }

        static MeterType findByName(final String s)
        {
            for (MeterType type : MeterType.values())
            {
                if (s.equalsIgnoreCase(type.getName()))
                {
                    return type;
                }
            }
            return null;
        }
    }

}
