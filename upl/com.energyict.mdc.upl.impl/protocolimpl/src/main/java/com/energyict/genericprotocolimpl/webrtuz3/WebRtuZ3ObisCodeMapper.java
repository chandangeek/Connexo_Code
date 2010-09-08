package com.energyict.genericprotocolimpl.webrtuz3;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.cosem.*;
import com.energyict.genericprotocolimpl.webrtu.common.obiscodemappers.ObisCodeMapper;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Copyrights EnergyICT
 * Date: 24-jun-2010
 * Time: 16:12:22
 */
public class WebRtuZ3ObisCodeMapper extends ObisCodeMapper {

    public WebRtuZ3ObisCodeMapper(CosemObjectFactory cosemObjectFactory) {
        super(cosemObjectFactory);
    }

    @Override
    public RegisterValue getRegisterValue(ObisCode obisCode) throws IOException {

        // Core firmware (not upgradeable)
        if (obisCode.toString().indexOf("0.0.0.2.0.255") != -1) {
            return ProtocolTools.setRegisterValueObisCode(super.getRegisterValue(ObisCode.fromString("1.0.0.2.0.255")), obisCode);
        }

        // Error register
        else if (obisCode.toString().indexOf("0.0.97.97.0.255") != -1) {
            GenericRead gr = getCosemObjectFactory().getGenericRead(obisCode, DLMSUtils.attrLN2SN(2), 1);
            String text = String.valueOf(gr.getValue());
            Quantity quantity = new Quantity(new BigDecimal(gr.getValue()), Unit.getUndefined());
            return new RegisterValue(obisCode, quantity, null, null, null, new Date(), 0, text);
        }

        // Alarm register
        else if (obisCode.toString().indexOf("0.0.97.98.0.255") != -1) {
            GenericRead gr = getCosemObjectFactory().getGenericRead(obisCode, DLMSUtils.attrLN2SN(2), 1);
            String text = String.valueOf(gr.getValue());
            Quantity quantity = new Quantity(new BigDecimal(gr.getValue()), Unit.getUndefined());
            return new RegisterValue(obisCode, quantity, null, null, null, new Date(), 0, text);
        }

        // Actif tarif code register
        else if (obisCode.toString().indexOf("0.0.96.14.0.255") != -1) {
            Data actifTarifCode = getCosemObjectFactory().getData(obisCode);
            Quantity quantity = new Quantity(actifTarifCode.getValue(), Unit.getUndefined());
            return new RegisterValue(obisCode, quantity);
        }

        else {
            return super.getRegisterValue(obisCode);
        }
    }

}
