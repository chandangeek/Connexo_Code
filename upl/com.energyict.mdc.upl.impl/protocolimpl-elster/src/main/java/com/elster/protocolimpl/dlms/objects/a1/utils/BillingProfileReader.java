/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elster.protocolimpl.dlms.objects.a1.utils;

import com.elster.dlms.cosem.applicationlayer.CosemApplicationLayer;
import com.elster.dlms.cosem.classes.class03.ScalerUnit;
import com.elster.dlms.cosem.classes.class03.Unit;
import com.elster.dlms.cosem.classes.class07.CaptureObjectDefinition;
import com.elster.dlms.cosem.classes.class07.EntryDescriptor;
import com.elster.dlms.cosem.classes.common.CosemClassIds;
import com.elster.dlms.cosem.simpleobjectmodel.SimpleCosemObject;
import com.elster.dlms.cosem.simpleobjectmodel.SimpleCosemObjectManager;
import com.elster.dlms.cosem.simpleobjectmodel.SimpleProfileObject;
import com.elster.dlms.cosem.simpleobjectmodel.SimpleRegisterObject;
import com.elster.dlms.types.basic.DlmsDateTime;
import com.elster.dlms.types.basic.ObisCode;
import com.elster.dlms.types.data.AbstractDlmsDataNumber;
import com.elster.dlms.types.data.DlmsData;
import com.elster.dlms.types.data.DlmsDataOctetString;
import com.elster.protocolimpl.dlms.objects.a1.utils.RegisterReader.RegisterResult;
import com.elster.protocolimpl.dlms.util.A1Defs;
import com.elster.protocolimpl.dlms.util.A1Utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Logger;

/**
 * @author heuckeg
 */
public class BillingProfileReader
{
    private final CosemApplicationLayer layer;
    private final Logger logger;
    //
    private SimpleProfileObject billingProfileObject;
    //
    private ScalerUnit[] scalers;
    //
    private HashMap<Integer, DlmsData[]> profileEntries;

    public BillingProfileReader(final CosemApplicationLayer layer, final Logger logger)
    {
        this.layer = layer;
        this.logger = logger;
    }

    public Object getValue(final ObisCode obisCode, final int hist)
    {
        try
        {
            int entry = hist - 101;
            int index = getProfileObject().indexOfCapturedObject(obisCode);

            DlmsData[] rawData = readProfileData(entry);
            if (rawData == null)
            {
                return null;
            }
            DlmsData data = rawData[index];

            if (ScalerUnit.isScalable(data))
            {
                ScalerUnit scaler = getScaler(index);
                if (scaler.getUnit() == null)
                {
                    return ((AbstractDlmsDataNumber) data).getValue();
                }
                return new RegisterResult(scaler.scale(data), scaler.getUnit());
            }

            if (isDateTimeObject(index))
            {
                DlmsDateTime ddt = new DlmsDateTime(((DlmsDataOctetString) data).getValue());
                return ddt.getUtcDate();
            }

            if (data instanceof DlmsDataOctetString)
            {
                return new String(((DlmsDataOctetString) data).getValue());
            }

        }
        catch (IOException ignore)
        {
        }
        return null;
    }

    private DlmsData[] readProfileData(int entry)
    {
        if (profileEntries.containsKey(entry))
        {
            return profileEntries.get(entry);
        }
        DlmsData[] rawData = null;
        try
        {
            if (readProfileEntry(entry) > 0)
            {
                rawData = new DlmsData[getProfileObject().getColumnCount()];
                for (int i = 0; i < getProfileObject().getColumnCount(); i++)
                {
                    rawData[i] = getProfileObject().getRawValue(0, i);
                }
            }
        }
        catch (IOException ignore)
        {
        }
        profileEntries.put(entry, rawData);
        return rawData;
    }

    private int readProfileEntry(final int entryToLoad) throws IOException
    {
        SimpleProfileObject profile = getProfileObject();
        if (profile.getEntriesInUse() <= entryToLoad)
        {
            return 0;
        }
        EntryDescriptor entryDescriptor = new EntryDescriptor(entryToLoad, entryToLoad, 1, profile.
                getColumnCount());

        return (int) profile.readProfileData(entryDescriptor, false, true);
    }

    private SimpleProfileObject getProfileObject() throws IOException
    {
        if (billingProfileObject == null)
        {
            SimpleCosemObjectManager objectManager = new SimpleCosemObjectManager(layer, A1Defs.DEFINITIONS);
            billingProfileObject = (SimpleProfileObject) objectManager.getSimpleCosemObject(
                    A1Defs.BILLING_PROFILE);
            scalers = new ScalerUnit[billingProfileObject.getColumnCount()];
            profileEntries = new HashMap<Integer, DlmsData[]>();
        }
        return billingProfileObject;
    }

    private ScalerUnit getScaler(final int index) throws IOException
    {
        if (scalers[index] == null)
        {
            final SimpleCosemObject relatedObject = getProfileObject().getRelatedObject(index);
            if (relatedObject instanceof SimpleRegisterObject)
            {
                if (relatedObject.getLogicalName().equals(A1Defs.VB_TOTAL_QUANTITY) ||
                        relatedObject.getLogicalName().equals(A1Defs.VAA))
                {
                    int scale = A1Utils.calculateScalerFromType(layer);
                    scalers[index] = new ScalerUnit(scale, Unit.CUBIC_METRE_CORRECTED_VOLUME);
                } else
                {
                    scalers[index] = ((SimpleRegisterObject) relatedObject).getScalerUnit();
                }
            } else
            {
                scalers[index] = new ScalerUnit(0, null);
            }
        }
        return scalers[index];
    }

    private boolean isDateTimeObject(int index) throws IOException
    {
        final CaptureObjectDefinition def = getProfileObject().getCaptureObjectDefinition(index);

        return (def.getAttributeIndex() == 2 && def.getClassId() == CosemClassIds.CLOCK) || (def.
                getAttributeIndex() == 5 && def.getClassId() == CosemClassIds.EXTENDED_REGISTER);
    }

}
