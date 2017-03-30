package com.energyict.protocolimpl.dlms.g3.registers.mapping;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.MemoryManagement;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocol.RegisterValue;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by iulian on 3/30/2017.
 */
public class MemoryManagementAttributesMapping  extends RegisterMapping{

    private static final int MIN_ATTR = 2;
    private static final int MAX_ATTR = 3;

    public MemoryManagementAttributesMapping(CosemObjectFactory cosemObjectFactory) {
        super(cosemObjectFactory);

    }

    public boolean canRead(ObisCode obisCode) {
        return (MemoryManagement.getDefaultObisCode().equalsIgnoreBillingField(obisCode) ||
                MemoryManagement.getLegacyObisCode().equalsIgnoreBillingField(obisCode)) &&
                (obisCode.getF() >= MIN_ATTR) &&
                (obisCode.getF() <= MAX_ATTR);
    }

    @Override
    protected RegisterValue doReadRegister(ObisCode obisCode) throws IOException {
        final MemoryManagement memoryManagement = getCosemObjectFactory().getMemoryManagement(obisCode);
        return parse(obisCode, readAttribute(obisCode, memoryManagement));
    }

    private AbstractDataType readAttribute(ObisCode obisCode, MemoryManagement memoryManagement) throws IOException {
        switch (obisCode.getF()){
            case 2:
                return memoryManagement.readMemoryStatistics();
            case 3:
                return memoryManagement.readFlashDevices();
            default:
                throw new NoSuchRegisterException("MemoryManagement attribute [" + obisCode.getF() + "] not supported!");
        }
    }

    public RegisterValue parse(ObisCode obisCode, AbstractDataType abstractDataType) throws NoSuchRegisterException {
        switch (obisCode.getF()){
            case 2:
                Structure structure = abstractDataType.getStructure();
                if (structure!=null){
                    JSONObject json = new JSONObject();
                    try {
                        String unit = structure.getDataType(3).getOctetString().stringValue();
                        json.put("usedDiskSpace", structure.getDataType(0).getInteger64().getValue()+" "+unit);
                        json.put("freeDiskSpace", structure.getDataType(1).getInteger64().getValue()+" "+unit);
                        json.put("totalDiskSpace", structure.getDataType(2).getInteger64().getValue()+" "+unit);
                        return new RegisterValue(obisCode, json.toString());
                    } catch (JSONException e) {
                        // swallow and return default
                    }
                }
                return new RegisterValue(obisCode, abstractDataType.toString());
            case 3:
                Array array = abstractDataType.getArray();
                if (array!=null){
                    StringBuilder sb = new StringBuilder();
                    for (AbstractDataType abd : array.getAllDataTypes()){
                        sb.append(abd.getOctetString().stringValue()).append(",");
                    }
                    return new RegisterValue(obisCode, sb.toString());
                }
                return new RegisterValue(obisCode, abstractDataType.toString());
            default:
                throw new NoSuchRegisterException("MemoryManagement attribute [" + obisCode.getF() + "] not supported!");

        }
    }

}
