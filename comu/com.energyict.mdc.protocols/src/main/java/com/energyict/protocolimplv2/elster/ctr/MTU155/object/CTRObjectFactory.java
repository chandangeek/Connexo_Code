package com.energyict.protocolimplv2.elster.ctr.MTU155.object;

import com.energyict.protocolimplv2.elster.ctr.MTU155.common.AttributeType;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRParsingException;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.field.CTRObjectID;
import com.energyict.protocolimplv2.elster.ctr.MTU155.primitive.CTRPrimitiveParser;

/**
 * Created by IntelliJ IDEA.
 * User: khe
 * Date: 21-sep-2010
 * Time: 15:01:20
 * Parses raw byte data and creates an object.
 */
public class CTRObjectFactory {

    public AbstractCTRObject parse(byte[] rawData, int offset, AttributeType type) throws CTRParsingException {
        CTRPrimitiveParser parser = new CTRPrimitiveParser();
        CTRObjectID id = parser.parseId(rawData, offset);
        return this.createObject(id, rawData, offset, type);
    }

    public AbstractCTRObject parse(byte[] rawData, int offset, AttributeType type, String objectId) throws CTRParsingException {
        CTRObjectID id = new CTRObjectID(objectId);
        byte[] objectData = new byte[(rawData.length - offset) + id.getLength()];
        if (type.hasIdentifier()) {
            System.arraycopy(id.getBytes(), 0, objectData, 0, id.getLength());
            System.arraycopy(rawData, offset, objectData, id.getLength(), rawData.length - offset);
            return this.createObject(id, objectData, 0, type);
        } else {
            return this.createObject(id, rawData, offset, type);
        }
    }

    private AbstractCTRObject createObject(CTRObjectID id, byte[] rawData, int offset, AttributeType type) throws CTRParsingException {

        AbstractCTRObject obj = null;

        switch (id.getX()) {
            case 1:
                obj = new FlowAndVolumeCategory(id);
                break;            //BIN value
            case 2:
                obj = new TotalizersCategory(id);
                break;            //BIN value
            case 3:
                obj = new EnergyCategory(id);
                break;            //BIN value
            case 4:
                obj = new PressureCategory(id);
                break;            //BIN value
            case 7:
                obj = new TemperatureCategory(id);
                break;            //BIN value -- 7.9.A ??
            case 8:
                obj = new DateAndTimeCategory(id);
                break;            //BIN value & Signed BIN
            case 9:
                obj = new EquipmentParametersCategory(id);
                break;            //String & BIN values
            case 0x0A:
                obj = new VolumeConverterCategory(id);
                break;            //BIN
            case 0x0B:
                obj = new GasAnalysisCategory(id);
                break;            //BIN
            case 0x0C:
                obj = new SystemMasterRecordCategory(id);
                break;            //BCD & BIN & String & Signed BIN
            case 0x0D:
                obj = new SecurityCategory(id);
                break;            //String & BIN values
            case 0x0E:
                obj = new CommunicationCategory(id);
                break;            //Signed BIN & String
            case 0x0F:
                obj = new MaintenanceCategory(id);
                break;            //BIN
            case 0x10:
                obj = new EventCategory(id);
                break;            //BIN
            case 0x11:
                obj = new ExecuteCategory(id);
                break;            //BIN
            case 18:
                obj = new StatusCategory(id);
                break;            //BIN
            case 19:
                obj = new InputOutputCategory(id);
                break;            //BIN
            case 21:
                obj = new TracesCategory(id);
                break;            //BIN
            case 23:
                obj = new CommercialParametersCategory(id);
                break;            //BIN
            case 24:
                obj = new BandTotalizerCategory(id);
                break;            //BIN
        }

        if (obj != null) {
            obj.parse(rawData, offset, type);
        } else {
            throw new CTRParsingException("Invalid Id: " + id.toString());
        }

        return obj;
    }


}
