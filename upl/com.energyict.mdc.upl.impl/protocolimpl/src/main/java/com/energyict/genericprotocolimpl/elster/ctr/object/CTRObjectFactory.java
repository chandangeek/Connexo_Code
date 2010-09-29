package com.energyict.genericprotocolimpl.elster.ctr.object;

import com.energyict.genericprotocolimpl.elster.ctr.primitive.CTRPrimitiveParser;

/**
 * Created by IntelliJ IDEA.
 * User: khe
 * Date: 21-sep-2010
 * Time: 15:01:20
 * Parses raw byte data and creates an object.
 */
public class CTRObjectFactory {

    public AbstractCTRObject parse(byte[] rawData, int offset) {
        CTRPrimitiveParser parser = new CTRPrimitiveParser();
        CTRObjectID id = parser.parseId(rawData, offset);
        return this.createObject(id, rawData, offset);
    }
    
    private AbstractCTRObject createObject(CTRObjectID id, byte[] rawData, int offset) {
 
        AbstractCTRObject obj = null;
        switch (id.getX()) {
            case 1: obj = new FlowAndVolumeCategory(id);            //BIN value
            case 2: obj = new TotalizersCategory(id);               //BIN value
            case 3: obj = new EnergyCategory(id);                   //BIN value             
            case 4: obj = new PressureCategory(id);                 //BIN value
            case 7: obj = new TemperatureCategory(id);              //BIN value -- 7.9.A ??               
            case 8: obj = new DateAndTimeCategory(id);              //BIN value & Signed BIN
            case 9: obj = new EquipmentParametersCategory(id);      //String & BIN values
            case 10: obj = new VolumeConverterCategory(id);         //BIN
            case 12: obj = new SystemMasterRecordCategory(id);      //BCD & BIN & String & Signed BIN
            case 13: obj = new SecurityCategory(id);                //String & BIN values
            case 14: obj = new CommunicationCategory(id);           //Signed BIN & String
            case 15: obj = new MaintenanceCategory(id);             //BIN
            case 16: obj = new EventCategory(id);                   //BIN
            case 17: obj = new ExecuteCategory(id);                 //BIN 
            case 18: obj = new StatusCategory(id);                  //BIN
            case 21: obj = new TracesCategory(id);                  //BIN
            case 24: obj = new BandTotalizerCategory(id);           //BIN
        }

        if (obj != null) {
            obj.parse(rawData, offset);
        }

        return obj;
    }


}
