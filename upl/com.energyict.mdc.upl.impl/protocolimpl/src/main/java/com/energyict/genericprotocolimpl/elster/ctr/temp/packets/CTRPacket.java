package com.energyict.genericprotocolimpl.elster.ctr.temp.packets;

import com.energyict.genericprotocolimpl.elster.ctr.temp.packets.fields.*;

/**
 * Copyrights EnergyICT
 * Date: 9-aug-2010
 * Time: 14:33:46
 */
public interface CTRPacket {

    byte STX = 0x0A;
    byte ETX = 0x0D;
    
    boolean isSMS();

    boolean isWakeUp();

    WakeUp getWakeUp();

    AddressField getAddressField();

    ClientProfile getClientProfile();

    FunctionCode getFunctionCode();

    Aleo getAleo();

    StructureCode getStructureCode();

    Channel getChannel();

    Cpa getCpa();

    Data getData();

    Crc getCrc();

    byte[] getBytes();

}
