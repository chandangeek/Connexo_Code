package com.energyict.protocolimpl.dlms.g3.registers;

import com.energyict.mdc.upl.NoSuchRegisterException;

import com.energyict.cbo.Unit;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.Disconnector;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.dlms.g3.registers.mapping.DisconnectControlAttributesMapping;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.util.Date;


/**
 * Copywrite EnergyICT 19.10.2016.
 */

public class DisconnectControlMapping extends G3Mapping {


        private DisconnectControlAttributesMapping disconnectControlAttributesMapping;

        protected DisconnectControlMapping(ObisCode obis) {
            super(obis);
        }

        @Override
        //Set the B-Filed to 0
        public ObisCode getBaseObisCode() {                 //Set the B-Filed to 0
            return ProtocolTools.setObisCodeField(super.getBaseObisCode(), 1, (byte) 0);
        }

        @Override
        public RegisterValue readRegister(CosemObjectFactory cosemObjectFactory) throws IOException {
            instantiateMappers(cosemObjectFactory);
            return readRegister(getObisCode());
        }

        private void instantiateMappers(CosemObjectFactory cosemObjectFactory) {
            if (disconnectControlAttributesMapping == null) {
                disconnectControlAttributesMapping = new DisconnectControlAttributesMapping(cosemObjectFactory);
            }
        }

        @Override
        public int getAttributeNumber() {
            int attributeValue; // indicates which attribute is being read
            switch (getObisCode().getB()) {
                case 1: attributeValue = 1;
                    break;
                case 2: attributeValue = 2;
                    break;
                case 3: attributeValue = 3;
                    break;
                case 4: attributeValue = 4;
                    break;
                default: attributeValue = 0;
            }
            return attributeValue;
        }

        @Override
        public RegisterValue parse(AbstractDataType abstractDataType, Unit unit, Date captureTime) throws IOException {
            instantiateMappers(null);  //Not used here

            if (disconnectControlAttributesMapping.canRead(getObisCode())) {
                return disconnectControlAttributesMapping.parse(getObisCode(), abstractDataType);
            }

            throw new NoSuchRegisterException("Register with obisCode [" + getObisCode() + "] not supported!");
        }

        private RegisterValue readRegister(final ObisCode obisCode) throws IOException {
            if (disconnectControlAttributesMapping.canRead(obisCode)) {
                return disconnectControlAttributesMapping.readRegister(obisCode);
            }
            throw new NoSuchRegisterException("Register with obisCode [" + obisCode + "] not supported!");
        }

        @Override
        public int getDLMSClassId() {
            if(getObisCode().equalsIgnoreBChannel(Disconnector.getDefaultObisCode()) ){
                return DLMSClassId.DISCONNECT_CONTROL.getClassId();
            } else {
                return -1;
            }
        }
    }


