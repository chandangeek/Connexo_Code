/*
 * AddressMap.java
 *
 * Created on 18 juni 2003, 15:30
 */

package com.energyict.protocolimpl.mbus.core.connection.iec870;

import java.util.ArrayList;
import java.util.List;

/**
 * implementation of the DataWatt IEC address mapping table, default table-0
 *
 * @author  Koen
 *
 */
public class AddressMap {

    public static final int MAX_ADDRESS=0x03FF;

    public static final List<AddressMap> ADDRESSES = new ArrayList<>();
    static {

        /*
         *  Following the doc from Datawatt, these addresses are without timetag!
         *  But after implementation of the protocol, it seems like these addresses represent values with timetag!
         */
        ADDRESSES.add(new AddressMap(0x0801,0x0BFF, "Melding with time tag",0x02));
        ADDRESSES.add(new AddressMap(0x0C01,0x0FFF, "Commando with time tag",0x02));
        ADDRESSES.add(new AddressMap(0x2801,0x28FF, "Meetw.Norm. with time tag",0x0A));
        ADDRESSES.add(new AddressMap(0x2901,0x29FF, "Meetw.Scaled with time tag",0x0C));
        ADDRESSES.add(new AddressMap(0x2A01,0x2AFF, "Meetw.Float with time tag",0x0E));
        ADDRESSES.add(new AddressMap(0x2C01,0x2CFF, "Setp.Norm. with time tag",0x0A));
        ADDRESSES.add(new AddressMap(0x2D01,0x2DFF, "Setp.Scaled with time tag",0x0C));
        ADDRESSES.add(new AddressMap(0x2E01,0x2EFF, "Setp.Float with time tag",0x0E));
        ADDRESSES.add(new AddressMap(0x4801,0x48FF, "Tellerstand with time tag",0x10));
        ADDRESSES.add(new AddressMap(0x6801,0x6BFF, "Double point with time tag",0x04));
        ADDRESSES.add(new AddressMap(0x6C01,0x6FFF, "Double comm. with time tag",0x04));

        /*
         *  Following the doc from Datawatt, these addresses are with timetag!
         *  But after implementation of the protocol, it seems like these addresses represent values without timetag!
         */
        ADDRESSES.add(new AddressMap(0x0001,0x03FF, "Melding without time tag",0x01));
        ADDRESSES.add(new AddressMap(0x0401,0x07FF, "Commando without time tag",0x01));
        ADDRESSES.add(new AddressMap(0x2001,0x20FF, "Meetw.Norm. without time tag",0x09));
        ADDRESSES.add(new AddressMap(0x2101,0x21FF, "Meetw.Scaled without time tag",0x0B));
        ADDRESSES.add(new AddressMap(0x2201,0x22FF, "Meetw.Float without time tag",0x0D));
        ADDRESSES.add(new AddressMap(0x2401,0x24FF, "Setp.Norm. without time tag",0x09));
        ADDRESSES.add(new AddressMap(0x2501,0x25FF, "Setp.Scaled without time tag",0x0B));
        ADDRESSES.add(new AddressMap(0x2601,0x26FF, "Setp.Float without time tag",0x0D));
        ADDRESSES.add(new AddressMap(0x4001,0x40FF, "Tellerstand without time tag",0x0F));
        ADDRESSES.add(new AddressMap(0x6001,0x63FF, "Double point without time tag",0x03));
        ADDRESSES.add(new AddressMap(0x6401,0x67FF, "Double comm. without time tag",0x03));

        ADDRESSES.add(new AddressMap(0x1401,0x17FF, "Commando command",0x2D));
        ADDRESSES.add(new AddressMap(0x3401,0x34FF, "Setp.Norm. command",0x30));
        ADDRESSES.add(new AddressMap(0x3501,0x35FF, "Setp.Scaled command",0x31));
        ADDRESSES.add(new AddressMap(0x3601,0x36FF, "Setp.Float command",0x32));
        ADDRESSES.add(new AddressMap(0x7401,0x77FF, "Double comm. command",0x2E));
    }

    int from,to,id;
    String type;
    /** Creates a new instance of AddressMap */
    public AddressMap(int from, int to, String type, int id) {
        this.from = from;
        this.to = to;
        this.type = type;
        this.id = id;
    }
    public int getFrom() {
        return from;
    }
    public int getTo() {
        return to;
    }
    public String getType() {
        return type;
    }
    public int getId() {
        return id;
    }
    public static AddressMap getAddressMapping(int address) {
        for (AddressMap a : ADDRESSES) {
            if ((address >= a.getFrom()) && (address <= a.getTo())) {
                return a;
            }
        }
        throw new IllegalArgumentException("Address "+address+" not found");
    }

}