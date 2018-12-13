package com.energyict.protocolimpl.dlms.g3.registers.mapping;

import com.energyict.mdc.upl.NoSuchRegisterException;
import com.energyict.mdc.upl.ProtocolException;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.Disconnector;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 19/10/2016
 */

public class DisconnectControlAttributesMapping extends RegisterMapping {

        private static final int MIN_ATTR = 1;
        private static final int MAX_ATTR = 4;

        public DisconnectControlAttributesMapping(CosemObjectFactory cosemObjectFactory) {
            super(cosemObjectFactory);
        }

        @Override
        public boolean canRead(final ObisCode obisCode) {
            return Disconnector.getDefaultObisCode().equalsIgnoreBAndEChannel(obisCode) &&
                    (obisCode.getB() >= MIN_ATTR) &&
                    (obisCode.getB() <= MAX_ATTR);
        }

        @Override
        protected RegisterValue doReadRegister(final ObisCode obisCode) throws IOException {
            final Disconnector disconnector = getCosemObjectFactory().getDisconnector(obisCode);
            return parse(obisCode, readAttribute(obisCode, disconnector));
        }

        protected AbstractDataType readAttribute(final ObisCode obisCode, Disconnector disconnector) throws IOException {
            switch (obisCode.getB()) {
                // Logical name
                case 1:
                    return OctetString.fromObisCode(Disconnector.getDefaultObisCode());
                case 2:
                    return disconnector.readOutputState();
                case 3:
                    return disconnector.readControlState();
                case 4:
                    return disconnector.readControlMode();
                default:
                    throw new NoSuchRegisterException("Disconnector attribute [" + obisCode.getB() + "] not supported!");
            }
        }

        @Override
        public RegisterValue parse(ObisCode obisCode, AbstractDataType abstractDataType) throws IOException {

            switch (obisCode.getB()) {
                // Logical name
                case 1:
                    return new RegisterValue(obisCode, Disconnector.getDefaultObisCode().toString());
                case 2:
                    return new RegisterValue(obisCode, "Output state: " + abstractDataType.getBooleanObject().getState());
                case 3:
                    return new RegisterValue(obisCode, " Control state: " + getControlStateString(abstractDataType));
                case 4:
                    return new RegisterValue(obisCode, " Control mode: " + abstractDataType.getTypeEnum().getValue());
                default:
                    throw new NoSuchRegisterException("PPP Setup attribute [" + obisCode.getB() + "] not supported!");
            }
        }

        public String getControlStateString(AbstractDataType disconnectorAttribute) throws IOException {
            StringBuffer builder = new StringBuffer();

            if (disconnectorAttribute.isTypeEnum()) {
                switch (disconnectorAttribute.getTypeEnum().getValue()) {
                    case 0: builder.append("DISCONNECTED");
                        break;
                    case 1: builder.append("CONNECTED");
                        break;
                    case 2: builder.append("READY_FOR_RECONNECTION");
                        break;
                    default: throw new ProtocolException("Invalid Disconnector Control State attribute value.");
                }
                return builder.toString();
            } else {
                throw new ProtocolException("Could not get correct LCP Options attribute format.");
            }
        }
    }



