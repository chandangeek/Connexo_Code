/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elster.protocolimpl.dlms.objects.a1;

import com.elster.dlms.cosem.applicationlayer.CosemApplicationLayer;
import com.elster.dlms.cosem.classes.class20.DayProfile;
import com.elster.dlms.cosem.classes.class20.WeekProfile;
import com.elster.dlms.cosem.classes.common.CosemClassIds;
import com.elster.dlms.types.basic.CosemAttributeDescriptor;
import com.elster.dlms.types.basic.DlmsDate;
import com.elster.dlms.types.basic.DlmsDateTime;
import com.elster.dlms.types.basic.DlmsTime;
import com.elster.dlms.types.basic.ObisCode;
import com.elster.dlms.types.data.DlmsDataArray;
import com.elster.dlms.types.data.DlmsDataOctetString;
import com.elster.dlms.types.data.DlmsDataStructure;

import java.io.IOException;
import java.util.Date;

/**
 *
 * @author heuckeg
 */
class TariffPassiveUpload implements IReadWriteObject
{

    public TariffPassiveUpload()
    {

    }

    private static final ObisCode obisCode = new ObisCode(0, 0, 13, 0, 0, 255);

    public ObisCode getObisCode()
    {
        return obisCode;
    }

    /**
     * Write tariff calendar
     *
     * @param layer - CosemApplicationLayer to use
     * @param param - parameter to function with param[0] - (String) calendar name param[1] - (Date)
     * activation time param[2] - DayProfile[] param[3] - WeekProfile[]
     * @throws IOException
     */
    public void write(CosemApplicationLayer layer, Object[] param) throws IOException
    {
        CosemAttributeDescriptor attributeDescriptor;

        // calendar name passive
        byte data[] = new byte[4];
        System.arraycopy(((String)param[0]).getBytes(), 0, data, 0, 3);
        data[3] = 0;

        DlmsDateTime activationTime = new DlmsDateTime(DlmsDate.NOT_SPECIFIED_DATE, DlmsTime.NOT_SPECIFIED_TIME);
        Date activationDate = (Date)param[1];
        if (activationDate != null)
        {
            activationTime = new DlmsDateTime(activationDate);
        }
        DlmsDate seasonStart = activationTime.getDlmsDate();

        DlmsDataOctetString calendarNamePassive = new DlmsDataOctetString(data);
        attributeDescriptor = new CosemAttributeDescriptor(obisCode, CosemClassIds.ACTIVITY_CALENDAR, 6);
        layer.setAttributeAndCheckResult(attributeDescriptor, calendarNamePassive);

        // season profile passive
        DlmsDataStructure season = new DlmsDataStructure(
                new DlmsDataOctetString(new byte[]
                        {
                            0x53, 0x31
                }),
                new DlmsDataOctetString(seasonStart.toBytes()),
                new DlmsDataOctetString(new byte[]
                        {
                            0x57, 0x31
                }));
        DlmsDataArray seasons = new DlmsDataArray(new DlmsDataStructure[]
        {
            season
        });
        attributeDescriptor = new CosemAttributeDescriptor(obisCode, CosemClassIds.ACTIVITY_CALENDAR, 7);
        layer.setAttributeAndCheckResult(attributeDescriptor, seasons);

        // week profile
        DlmsDataArray wp = new DlmsDataArray((WeekProfile[])param[3]);
        attributeDescriptor = new CosemAttributeDescriptor(obisCode, CosemClassIds.ACTIVITY_CALENDAR, 8);
        layer.setAttributeAndCheckResult(attributeDescriptor, wp);

        // day profiles
        DlmsDataArray dp = new DlmsDataArray((DayProfile[])param[2]);
        attributeDescriptor = new CosemAttributeDescriptor(obisCode, CosemClassIds.ACTIVITY_CALENDAR, 9);
        layer.setAttributeAndCheckResult(attributeDescriptor, dp);

        // activation date
        attributeDescriptor = new CosemAttributeDescriptor(obisCode, CosemClassIds.ACTIVITY_CALENDAR, 10);
        layer.setAttributeAndCheckResult(attributeDescriptor, new DlmsDataOctetString(activationTime.toBytes()));
    }

    public Object read(CosemApplicationLayer layer) throws IOException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
