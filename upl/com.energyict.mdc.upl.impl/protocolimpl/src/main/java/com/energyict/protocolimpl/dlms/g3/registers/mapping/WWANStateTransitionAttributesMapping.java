package com.energyict.protocolimpl.dlms.g3.registers.mapping;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.WWANStateTransitionIC;
import com.energyict.mdc.upl.NoSuchRegisterException;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class WWANStateTransitionAttributesMapping extends RegisterMapping {

    private static final int MIN_ATTR = 1;
    private static final int MAX_ATTR = 2;
    private static ObjectMapper mapper = new ObjectMapper();

    public WWANStateTransitionAttributesMapping(CosemObjectFactory cosemObjectFactory) {
        super(cosemObjectFactory);
    }

    @Override
    public boolean canRead(final ObisCode obisCode) {
        return WWANStateTransitionIC.getDefaultObisCode().equalsIgnoreBAndEChannel(obisCode) &&
                (obisCode.getE() >= MIN_ATTR) &&
                (obisCode.getE() <= MAX_ATTR);
    }

    @Override
    protected RegisterValue doReadRegister(final ObisCode obisCode) throws IOException {
        final WWANStateTransitionIC wwanStateTransitionIC = getCosemObjectFactory().getWWANStateTransitionIC(obisCode);
        return parse(obisCode, readAttribute(obisCode, wwanStateTransitionIC));
    }

    protected AbstractDataType readAttribute(final ObisCode obisCode, WWANStateTransitionIC wwanStateTransitionIC) throws IOException {
        switch (obisCode.getE()) {
            case 2:
                return wwanStateTransitionIC.readLastTransition();
            default:
                throw new NoSuchRegisterException("WWAN state transition attribute [" + obisCode.getE() + "] not supported!");
        }
    }

    @Override
    public RegisterValue parse(ObisCode obisCode, AbstractDataType abstractDataType) throws IOException {
        switch (obisCode.getE()) {
            case 2:
                Structure lastTransition = abstractDataType.getStructure();
                String result = parseLastStateTransitionStructure(lastTransition);
                return new RegisterValue(obisCode, result);
            default:
                throw new NoSuchRegisterException("WWAN state transition attribute [" + obisCode.getE() + "] not supported!");
        }
    }

    private String parseLastStateTransitionStructure(Structure structure) throws IOException {
        final LastStateTransition lastStateTransition = new LastStateTransition(structure);
        return mapper.writeValueAsString(lastStateTransition);
    }

    class LastStateTransition {
        public String timestamp;
        public String from_state;
        public String to_state;
        public String reason;

        public LastStateTransition(Structure structure) throws ProtocolException {
            timestamp = new AXDRDateTime(structure.getDataType(0).getOctetString().getBEREncodedByteArray()).getValue().getTime().toString();
            from_state = ModemState.getDescription( structure.getDataType(1).getTypeEnum().getValue() );
            to_state = ModemState.getDescription( structure.getDataType(2).getTypeEnum().getValue() );
            reason = structure.getDataType(3).getOctetString().stringValue();
        }
    }

    enum ModemState {

        STOPPED(0, "stopped"),
        UNCONFIGURED(1, "unconfigured"),
        CONFIGURED(2, "configured"),
        CONNECTING(3, "connecting"),
        CONNECTED(4, "connected"),
        DISCONNECTING(5, "disconnecting"),
        UNKNOWN(255, "unknown");

        int code;
        String description;

        ModemState(int code, String description) {
            this.code = code;
            this.description = description;
        }

        public static String getDescription(int code) {
            for (ModemState ms : values()) {
                if (ms.code == code) {
                    return code + "=" + ms.description;
                }
            }
            throw new EnumConstantNotPresentException(ModemState.class, "Code " + code + " not present in enumeration.");
        }
    }

}
