package com.energyict.genericprotocolimpl.webrtuz3;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.cosem.*;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

/**
 * Copyrights EnergyICT
 * Date: 7-jan-2011
 * Time: 8:43:42
 */
public class WebRtuZ3BulkObisCodeMapper extends WebRtuZ3ObisCodeMapper {

    private final List<ObisCode> enabledObisCodes = new ArrayList<ObisCode>();

    private static final Map<ObisCode, DLMSAttribute> BULKMAPPINGS = new HashMap<ObisCode, DLMSAttribute>();
    private ComposedCosemObject composedCosemObject;

    public WebRtuZ3BulkObisCodeMapper(CosemObjectFactory cosemObjectFactory) {
        super(cosemObjectFactory);
    }

    static {
        BULKMAPPINGS.put(CORE_FW_VERSION, DLMSAttribute.fromString("?:1.0.0.2.0.255:2"));
        BULKMAPPINGS.put(ERROR_REGISTER, DLMSAttribute.fromString("?:0.0.97.97.0.255:2"));
        BULKMAPPINGS.put(ALARM_REGISTER, DLMSAttribute.fromString("?:0.0.97.97.0.255:2"));
        BULKMAPPINGS.put(ACTIVE_TARIF_REGISTER, DLMSAttribute.fromString("1:0.0.96.14.0.255:2"));
        BULKMAPPINGS.put(ACTIVITY_CALENDAR, DLMSAttribute.fromString("20:0.0.13.0.0.255:2"));
    }

    public void enableRegisterMapping(ObisCode obisCode) {
        if (isBulkSupportedForObis(obisCode)) {
            getEnabledObisCodes().add(obisCode);
        }
    }

    public boolean isBulkSupportedForObis(ObisCode obisCode) {
        for (ObisCode obisFromMap : getBulkmappings().keySet()) {
            if (obisFromMap.equals(obisCode)) {
                return true;
            }
        }
        return false;
    }

    public boolean isEnabled(ObisCode obisCode) {
        for (ObisCode enabledObisCode : getEnabledObisCodes()) {
            if (enabledObisCode.equals(obisCode)) {
                return true;
            }
        }
        return false;
    }

    public List<ObisCode> getEnabledObisCodes() {
        return enabledObisCodes;
    }

    public static Map<ObisCode, DLMSAttribute> getBulkmappings() {
        return BULKMAPPINGS;
    }

    public ComposedCosemObject getComposedCosemObject() {
        if (composedCosemObject == null) {
            composedCosemObject = getCosemObjectFactory().getComposedCosemObject(getDLMSAttributes());
        }
        return composedCosemObject;
    }

    private List<DLMSAttribute> getDLMSAttributes() {
        List<DLMSAttribute> attributes = new ArrayList<DLMSAttribute>();
        for (ObisCode obisCode : enabledObisCodes) {
            DLMSAttribute attribute = getBulkmappings().get(obisCode);
            if (attribute != null) {
                attributes.add(attribute);
            }
        }
        return attributes;
    }

    public AbstractDataType getAttribute(ObisCode obisCode) throws IOException {
        return getComposedCosemObject().getAttribute(getBulkmappings().get(obisCode));
    }

    private GenericRead getAttributeAsGenericRead(ObisCode obisCode) throws IOException {
        return getComposedCosemObject().getAttributeAsGenericRead(getBulkmappings().get(obisCode));
    }

    @Override
    public RegisterValue getRegisterValue(ObisCode obisCode) throws IOException {
        if (isEnabled(obisCode)) {

            // Core firmware (not upgradeable)
            if (CORE_FW_VERSION.equals(obisCode)) {
                return new RegisterValue(obisCode, null, null, null, null, new Date(), 0,
                        getAttributeAsGenericRead(obisCode).getString());
            }

            // Error register
            else if (ERROR_REGISTER.equals(obisCode)) {
                GenericRead gr = getAttributeAsGenericRead(obisCode);
                String text = String.valueOf(gr.getValue());
                Quantity quantity = new Quantity(new BigDecimal(gr.getValue()), Unit.getUndefined());
                return new RegisterValue(obisCode, quantity, null, null, null, new Date(), 0, text);
            }

            // Alarm register
            else if (ALARM_REGISTER.equals(obisCode)) {
                GenericRead gr = getAttributeAsGenericRead(obisCode);
                String text = String.valueOf(gr.getValue());
                Quantity quantity = new Quantity(new BigDecimal(gr.getValue()), Unit.getUndefined());
                return new RegisterValue(obisCode, quantity, null, null, null, new Date(), 0, text);
            }

            // Active tarif code register
            else if (ACTIVE_TARIF_REGISTER.equals(obisCode)) {
                GenericRead actifTarifCode = getAttributeAsGenericRead(obisCode);
                Quantity quantity = new Quantity(actifTarifCode.getValue(), Unit.getUndefined());
                return new RegisterValue(obisCode, quantity);
            }

            // Active tarif code register
            else if (ACTIVITY_CALENDAR.equals(obisCode)) {
                AbstractDataType attribute = getAttribute(obisCode);
                if (attribute instanceof OctetString) {
                    return new RegisterValue(obisCode, null, null, null, null, new Date(), 0,
                            new String(((OctetString) attribute).getOctetStr()));
                }
            }

        }
        return super.getRegisterValue(obisCode);
    }

}
