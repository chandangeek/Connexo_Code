package com.energyict.protocolimpl.dlms.g3.registers.mapping;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.GSMDiagnosticsIC;
import com.energyict.mdc.upl.NoSuchRegisterException;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;

/**
 * Created by H165680 on 17/04/2017.
 */
public class GSMDiagnosticsAttributesMapping extends RegisterMapping {

    private static final int MIN_ATTR = 2;
    private static final int MAX_ATTR = 254;
    private static ObjectMapper mapper = new ObjectMapper();

    public GSMDiagnosticsAttributesMapping(CosemObjectFactory cosemObjectFactory) {
        super(cosemObjectFactory);
    }

    @Override
    public boolean canRead(final ObisCode obisCode) {
        return GSMDiagnosticsIC.getDefaultObisCode().equalsIgnoreBAndEChannel(obisCode) &&
                (obisCode.getE() >= MIN_ATTR) &&
                (obisCode.getE() <= MAX_ATTR);
    }

    @Override
    protected RegisterValue doReadRegister(final ObisCode obisCode) throws IOException {
        final GSMDiagnosticsIC gsmDiagnosticsIC = getCosemObjectFactory().getGSMDiagnosticsIC(obisCode);
        return parse(obisCode, readAttribute(obisCode, gsmDiagnosticsIC));
    }

    protected AbstractDataType readAttribute(final ObisCode obisCode, GSMDiagnosticsIC gsmDiagnosticsIC) throws IOException {
        switch (obisCode.getE()) {
            case 2:
                return gsmDiagnosticsIC.readOperator();
            case 3:
                return gsmDiagnosticsIC.readStatus();
            case 4:
                return gsmDiagnosticsIC.readCSAttachment();
            case 5:
                return gsmDiagnosticsIC.readPSStatus();
            case 6:
                return gsmDiagnosticsIC.readCellInfo();
            case 7:
                return gsmDiagnosticsIC.readAdjacentCells();
            case 8:
                return gsmDiagnosticsIC.readCaptureTime();
            case 254:
                return gsmDiagnosticsIC.readModemType();
            case 253:
                return gsmDiagnosticsIC.readModemVersion();
            case 252:
                return gsmDiagnosticsIC.readIMEI();
            case 251:
                return gsmDiagnosticsIC.readIMSI();
            case 250:
                return gsmDiagnosticsIC.readSimCardId();
            case 249:
                return gsmDiagnosticsIC.readMSISDNNumber();
            case 248:
                return gsmDiagnosticsIC.readTotalTXBytes();
            case 247:
                return gsmDiagnosticsIC.readTotalRXBytes();
            default:
                throw new NoSuchRegisterException("GSM Diagnostics attribute [" + obisCode.getE() + "] not supported!");

        }
    }

    @Override
    public RegisterValue parse(ObisCode obisCode, AbstractDataType abstractDataType) throws IOException {

        switch (obisCode.getE()) {
            case 2:
                return new RegisterValue(obisCode, abstractDataType.getVisibleString().getStr());
            case 3:
                int status = abstractDataType.getTypeEnum().getValue();
                return new RegisterValue(obisCode, status + "=" + GSMDiagnosticsStatus.getDescription(status));
            case 4:
                int csAttachement = abstractDataType.getTypeEnum().getValue();
                return new RegisterValue(obisCode, csAttachement + "=" + CSAttachment.getDescription(csAttachement));
            case 5:
                int psStatus = abstractDataType.getTypeEnum().getValue();
                return new RegisterValue(obisCode, psStatus + "=" + PSStatus.getDescription(psStatus));
            case 6:
                return new RegisterValue(obisCode, parseCellInfoStructure(abstractDataType.getStructure()));
            case 7:
                return new RegisterValue(obisCode, parseAdjacentCells(abstractDataType.getArray()));
            case 8:
                // TODO device timezone?
                return new RegisterValue(obisCode, new AXDRDateTime(abstractDataType.getOctetString().getBEREncodedByteArray()).getValue().getTime().toString());
            case 254:
                return new RegisterValue(obisCode, abstractDataType.getOctetString().stringValue());
            case 253:
                return new RegisterValue(obisCode, abstractDataType.getOctetString().stringValue());
            case 252:
                return new RegisterValue(obisCode, abstractDataType.getOctetString().stringValue());
            case 251:
                return new RegisterValue(obisCode, abstractDataType.getOctetString().stringValue());
            case 250:
                return new RegisterValue(obisCode, abstractDataType.getOctetString().stringValue());
            case 249:
                return new RegisterValue(obisCode, new Quantity(abstractDataType.getInteger64().toBigDecimal(), Unit.get("")));
            case 248:
                return new RegisterValue(obisCode, new Quantity(abstractDataType.getInteger64().toBigDecimal(), Unit.get("")));
            case 247:
                return new RegisterValue(obisCode, new Quantity(abstractDataType.getInteger64().toBigDecimal(), Unit.get("")));
            default:
                throw new NoSuchRegisterException("GSM Diagnostics attribute [" + obisCode.getE() + "] not supported!");

        }
    }

    private String parseCellInfoStructure(Structure structure) throws IOException {
        final CellInfo cellInfo = new CellInfo(structure);
        return mapper.writeValueAsString(cellInfo);
    }

    class CellInfo {
        public double cell_ID;
        public long   location_ID;
        public int    signal_quality;
        public int    ber;
        public long   mcc;
        public long   mnc;
        public double channel_number;

        public CellInfo(Structure structure) throws IOException {
            cell_ID = structure.getDataType(0, Unsigned32.class).longValue();
            location_ID = structure.getDataType(1, Unsigned16.class).longValue();
            signal_quality = structure.getDataType(2, Unsigned8.class).getValue();
            ber = structure.getDataType(3, Unsigned8.class).getValue();
            mcc = structure.getDataType(4, Unsigned16.class).longValue();
            mnc = structure.getDataType(5, Unsigned16.class).longValue();
            channel_number = structure.getDataType(6, Unsigned32.class).longValue();
        }
    }

    private String parseAdjacentCells(Array array) throws IOException {
        final AdjacentCells adjacentCells = new AdjacentCells(array);
        return mapper.writeValueAsString(adjacentCells);
    }

    class AdjacentCells {
        public List<AdjacentCellInfo> adjacentCellInfoList;

        public AdjacentCells(Array array) throws IOException {
            for (int i = 0; i < array.nrOfDataTypes(); i++) {
                adjacentCellInfoList.add(
                        new AdjacentCellInfo( array.getDataType(i, Structure.class) )
                );
            }
        }
    }

    class AdjacentCellInfo {
        public double cell_ID;
        public int    signal_quality;

        public AdjacentCellInfo(Structure structure) throws IOException {
            cell_ID = structure.getDataType(0, Unsigned32.class).longValue();
            signal_quality = structure.getDataType(1, Unsigned8.class).getValue();
        }
    }

    enum GSMDiagnosticsStatus {

        NOT_REGISTERED(0, "not registered"),
        REGISTERED(1, "registered, home network"),
        NOT_REGISTERED_SEARCHING_OPERATOR(2, "not registered, but MT is currently searching a new operator to register to"),
        REGISTRATION_DENIED(3, "registration denied"),
        UNKNOWN(4, "unknown"),
        REGISTERED_ROAMING(5, "registered, roaming");
        // 6 to 255 reserved, update this as necessary
        static final int RESERVED_LIMIT = 6;

        int id;
        String description;

        GSMDiagnosticsStatus(int id, String description) {
            this.id = id;
            this.description = description;
        }

        public static String getDescription(int id) {
            if (id < RESERVED_LIMIT) {
                for (GSMDiagnosticsStatus status : values()) {
                    if (status.id == id) {
                        return status.description;
                    }
                }
                throw new EnumConstantNotPresentException(GSMDiagnosticsStatus.class, "Description for status " + id  + " not found");
            } else {
                return "reserved";
            }
        }

    }

    enum CSAttachment {

        INACTIVE(0, "inactive"),
        INCOMING_CALL(1, "incoming call"),
        ACTIVE(2, "active");
        // 3 to 255 reserved, update this as necessary
        static final int RESERVED_LIMIT = 3;

        int id;
        String description;

        CSAttachment(int id, String description) {
            this.id = id;
            this.description = description;
        }

        public static String getDescription(int id) {
            if (id < RESERVED_LIMIT) {
                for (CSAttachment csAttachment : values()) {
                    if (csAttachment.id == id) {
                        return csAttachment.description;
                    }
                }
                throw new EnumConstantNotPresentException(CSAttachment.class, "Description for cs_attachment " + id  + " not found");
            } else {
                return "reserved";
            }
        }

    }

    enum PSStatus {

        INACTIVE(0, "inactive"),
        GPRS(1, "GPRS"),
        EDGE(2, "EDGE"),
        UMTS(3, "UMTS"),
        HSDPA(4, "HSDPA"),
        LTE(5, "LTE"),
        CDMA(6, "CDMA");
        // 7 to 255 reserved, update this as necessary
        static final int RESERVED_LIMIT = 7;

        int id;
        String description;

        PSStatus(int id, String description) {
            this.id = id;
            this.description = description;
        }

        public static String getDescription(int id) {
            if (id < RESERVED_LIMIT) {
                for (PSStatus status : values()) {
                    if (status.id == id) {
                        return status.description;
                    }
                }
                throw new EnumConstantNotPresentException(PSStatus.class, "Description for ps_status " + id  + " not found");
            } else {
                return "reserved";
            }
        }

    }

}
