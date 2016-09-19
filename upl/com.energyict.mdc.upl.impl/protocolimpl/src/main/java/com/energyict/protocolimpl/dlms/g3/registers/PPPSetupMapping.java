package com.energyict.protocolimpl.dlms.g3.registers;

import com.energyict.cbo.Unit;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.GPRSModemSetup;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.dlms.g3.registers.mapping.PPPSetupAttributesMapping;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.util.Date;

/**
 * Copywrite EnergyICT 22.09.2016.
 */

public class PPPSetupMapping extends G3Mapping {


        private PPPSetupAttributesMapping pppSetupAttributesMapping;

        protected PPPSetupMapping(ObisCode obis) {
            super(obis);
        }

        @Override
        //Set the B-Filed to 0
        public ObisCode getBaseObisCode() {                 //Set the E-Filed to 0
            return ProtocolTools.setObisCodeField(super.getBaseObisCode(), 2, (byte) 0);
        }

        @Override
        public RegisterValue readRegister(CosemObjectFactory cosemObjectFactory) throws IOException {
            instantiateMappers(cosemObjectFactory);
            return readRegister(getObisCode());
        }

        private void instantiateMappers(CosemObjectFactory cosemObjectFactory) {
            if (pppSetupAttributesMapping == null) {
                pppSetupAttributesMapping = new PPPSetupAttributesMapping(cosemObjectFactory);
            }
        }

        @Override
        public int getAttributeNumber() {
            return getObisCode().getB();        //The B-field of the obiscode indicates which attribute is being read
        }

        @Override
        public RegisterValue parse(AbstractDataType abstractDataType, Unit unit, Date captureTime) throws IOException {
            instantiateMappers(null);  //Not used here

            if (pppSetupAttributesMapping.canRead(getObisCode())) {
                return pppSetupAttributesMapping.parse(getObisCode(), abstractDataType);
            }

            throw new NoSuchRegisterException("Register with obisCode [" + getObisCode() + "] not supported!");
        }

        private RegisterValue readRegister(final ObisCode obisCode) throws IOException {
            if (pppSetupAttributesMapping.canRead(obisCode)) {
                return pppSetupAttributesMapping.readRegister(obisCode);
            }
            throw new NoSuchRegisterException("Register with obisCode [" + obisCode + "] not supported!");
        }

        @Override
        public int getDLMSClassId() {
            if(getObisCode().equalsIgnoreBChannel(GPRSModemSetup.getDefaultObisCode()) ){
                return DLMSClassId.GPRS_SETUP.getClassId();
            } else {
                return -1;
            }
        }
    }

