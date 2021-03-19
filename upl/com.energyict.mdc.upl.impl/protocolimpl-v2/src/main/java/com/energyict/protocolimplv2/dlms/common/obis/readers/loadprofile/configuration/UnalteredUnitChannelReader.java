package com.energyict.protocolimplv2.dlms.common.obis.readers.loadprofile.configuration;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;
import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.ScalerUnit;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.cosem.ComposedCosemObject;
import com.energyict.dlms.cosem.attributes.RegisterAttributes;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;

import java.io.IOException;

/**
 * This should be used when channels in load profile has same unit as the one in obis code table (same as the obis it points to).
 */
public class UnalteredUnitChannelReader extends AbstractChannelInfoReader {

    @Override
    protected Unit getUnit(AbstractDlmsProtocol protocol, ObisCode obisCode) {
        try {
            UniversalObject uo = DLMSUtils.findCosemObjectInObjectList(protocol.getDlmsSession().getMeterConfig().getInstantiatedObjectList(), obisCode);
            if (uo == null) {
                throw new IOException("Could not determine unit for obis code:" + obisCode);
            }
            DLMSAttribute dlmsAttribute = new DLMSAttribute(obisCode, RegisterAttributes.SCALER_UNIT.getAttributeNumber(), uo.getClassID());
            ComposedCosemObject cosemObject = new ComposedCosemObject(protocol.getDlmsSession(), false, dlmsAttribute);
            AbstractDataType attribute = cosemObject.getAttribute(dlmsAttribute);
            if (attribute.isStructure()) {
                return new ScalerUnit(attribute.getStructure()).getEisUnit();
            }
        } catch (IOException e) {
            protocol.journal("Could not determine unit! Reading DLMS attribute failed:" + e.getMessage());
        }
        return Unit.get(BaseUnit.UNITLESS);
    }

}
