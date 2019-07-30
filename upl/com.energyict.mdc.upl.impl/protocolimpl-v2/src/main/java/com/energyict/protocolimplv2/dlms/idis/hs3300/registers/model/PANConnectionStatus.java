package com.energyict.protocolimplv2.dlms.idis.hs3300.registers.model;

import com.energyict.dlms.axrdencoding.BitString;
import com.energyict.dlms.axrdencoding.BooleanObject;
import com.energyict.dlms.axrdencoding.Integer8;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.TypeEnum;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.axrdencoding.Unsigned8;

import java.io.IOException;

public class PANConnectionStatus {

    // routing table
    public long destination_address;
    public long next_hop_address;
    public long route_cost;
    public int  hop_count;
    public int  weak_link_count;
    public long valid_time;

    // neighbour table
    public long    short_address;
    public boolean payload_modulation_scheme;
    public String  tone_map;
    public String  modulation;
    public int     tx_gain;
    public String  tx_res;
    public String  tx_coeff;
    public int     reverse_lqi;
    public int     phase_differential;
    public int     tmr_valid_time;
    public int     neighbour_valid_time;

    public PANConnectionStatus(Structure structure) throws IOException {

        // routing table
        final Structure routingTable = structure.getDataType(0).getStructure();

        destination_address = routingTable.getDataType(0, Unsigned16.class).longValue();
        next_hop_address    = routingTable.getDataType(1, Unsigned16.class).longValue();
        route_cost          = routingTable.getDataType(2, Unsigned16.class).longValue();
        hop_count           = routingTable.getDataType(3, Unsigned8.class).getValue();
        weak_link_count     = routingTable.getDataType(4, Unsigned8.class).getValue();
        valid_time          = routingTable.getDataType(5, Unsigned16.class).longValue();

        // neighbour table
        final Structure neighbourTable = structure.getDataType(1).getStructure();

        short_address             = neighbourTable.getDataType(0, Unsigned16.class).longValue();
        payload_modulation_scheme = neighbourTable.getDataType(1, BooleanObject.class).getState();
        tone_map                  = neighbourTable.getDataType(2, BitString.class).toString();
        modulation                = ModulationType.getDescription( neighbourTable.getDataType(3, TypeEnum.class).getValue() );
        tx_gain                   = neighbourTable.getDataType(4, Integer8.class).getValue();
        tx_res                    = TXResType.getDescription( neighbourTable.getDataType(5, TypeEnum.class).getValue() );
        tx_coeff                  = neighbourTable.getDataType(6, BitString.class).toString();
        reverse_lqi               = neighbourTable.getDataType(7, Unsigned8.class).getValue();
        phase_differential        = neighbourTable.getDataType(8, Integer8.class).getValue();
        tmr_valid_time            = neighbourTable.getDataType(9, Unsigned8.class).getValue();
        neighbour_valid_time      = neighbourTable.getDataType(10, Unsigned8.class).getValue();
    }

}
