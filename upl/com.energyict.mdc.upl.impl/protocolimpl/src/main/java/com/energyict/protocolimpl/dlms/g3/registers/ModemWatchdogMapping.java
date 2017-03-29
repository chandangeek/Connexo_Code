package com.energyict.protocolimpl.dlms.g3.registers;

import com.energyict.cbo.Unit;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.ModemWatchdogConfiguration;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.dlms.g3.registers.mapping.ModemWatchdogAttributesMapping;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.util.Date;

/**
 * Copywrite EnergyICT 22.09.2016.
 */

public class ModemWatchdogMapping extends G3Mapping {

        private ModemWatchdogAttributesMapping modemWatchdogAttributesMapping;

        protected ModemWatchdogMapping(ObisCode obis) {
            super(obis);
        }

        @Override
        public ObisCode getBaseObisCode() {                 //Set the F-Filed to 255
            return ProtocolTools.setObisCodeField(super.getBaseObisCode(), 5, (byte) 255);
        }

        @Override
        public RegisterValue readRegister(CosemObjectFactory cosemObjectFactory) throws IOException {
            instantiateMappers(cosemObjectFactory);
            return readRegister(getObisCode());
        }

        private void instantiateMappers(CosemObjectFactory cosemObjectFactory) {
            if (modemWatchdogAttributesMapping == null) {
                modemWatchdogAttributesMapping = new ModemWatchdogAttributesMapping(cosemObjectFactory);
            }
        }

        @Override
        public int getAttributeNumber() {
            int attributeValue; // indicates which attribute is being read
            switch (getObisCode().getF()) {
                case 1: attributeValue = 1;
                    break;
                case 2: attributeValue = 2;
                    break;
                case 3: attributeValue = 3;
                    break;
                default: attributeValue = 0;
            }
            return attributeValue;
        }

        @Override
        public RegisterValue parse(AbstractDataType abstractDataType, Unit unit, Date captureTime) throws IOException {
            instantiateMappers(null);  //Not used here

            if (modemWatchdogAttributesMapping.canRead(getObisCode())) {
                return modemWatchdogAttributesMapping.parse(getObisCode(), abstractDataType);
            }

            throw new NoSuchRegisterException("Register with obisCode [" + getObisCode() + "] not supported!");
        }

        private RegisterValue readRegister(final ObisCode obisCode) throws IOException {
            if (modemWatchdogAttributesMapping.canRead(obisCode)) {
                return modemWatchdogAttributesMapping.readRegister(obisCode);
            }
            throw new NoSuchRegisterException("Register with obisCode [" + obisCode + "] not supported!");
        }

        @Override
        public int getDLMSClassId() {
            if(getObisCode().equalsIgnoreBillingField(ModemWatchdogConfiguration.getDefaultObisCode()) ){
                return DLMSClassId.MODEM_WATCHDOG_SETUP.getClassId();
            } else {
                return -1;
            }
        }
    }


