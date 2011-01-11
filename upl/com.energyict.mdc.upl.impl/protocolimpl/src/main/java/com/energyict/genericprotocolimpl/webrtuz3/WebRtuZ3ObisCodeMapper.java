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

    public static final ObisCode CORE_FW_VERSION = ObisCode.fromString("0.0.0.2.0.255");
    public static final ObisCode ERROR_REGISTER = ObisCode.fromString("0.0.97.97.0.255");
    public static final ObisCode ALARM_REGISTER = ObisCode.fromString("0.0.97.98.0.255");
    public static final ObisCode ACTIVE_TARIF_REGISTER = ObisCode.fromString("0.0.96.14.0.255");

    public WebRtuZ3ObisCodeMapper(CosemObjectFactory cosemObjectFactory) {
        super(cosemObjectFactory);
    }

    @Override
    public RegisterValue getRegisterValue(ObisCode obisCode) throws IOException {

        // Core firmware (not upgradeable)
        if (CORE_FW_VERSION.equals(obisCode)) {
            return ProtocolTools.setRegisterValueObisCode(super.getRegisterValue(ObisCode.fromString("1.0.0.2.0.255")), obisCode);
        }

        // Error register
        else if (ERROR_REGISTER.equals(obisCode)) {
            GenericRead gr = getCosemObjectFactory().getGenericRead(obisCode, DLMSUtils.attrLN2SN(2), 1);
            String text = String.valueOf(gr.getValue());
            Quantity quantity = new Quantity(new BigDecimal(gr.getValue()), Unit.getUndefined());
            return new RegisterValue(obisCode, quantity, null, null, null, new Date(), 0, text);
        }

        // Alarm register
        else if (ALARM_REGISTER.equals(obisCode)) {
            GenericRead gr = getCosemObjectFactory().getGenericRead(obisCode, DLMSUtils.attrLN2SN(2), 1);
            String text = String.valueOf(gr.getValue());
            Quantity quantity = new Quantity(new BigDecimal(gr.getValue()), Unit.getUndefined());
            return new RegisterValue(obisCode, quantity, null, null, null, new Date(), 0, text);
        }

        // Active tarif code register
        else if (ACTIVE_TARIF_REGISTER.equals(obisCode)) {
            Data actifTarifCode = getCosemObjectFactory().getData(obisCode);
            Quantity quantity = new Quantity(actifTarifCode.getValue(), Unit.getUndefined());
            return new RegisterValue(obisCode, quantity);
        } else {
            return super.getRegisterValue(obisCode);
        }
    }

}
