package com.energyict.protocolimpl.elster.ctr.packets;

import com.energyict.protocolimpl.elster.ctr.packets.fields.*;

/**
 * Copyrights EnergyICT
 * Date: 9-aug-2010
 * Time: 14:33:46
 */
public interface CTRPacket {

    boolean isSMS();

    boolean isWakeUp();

    WakeUp getWakeUp();

    AddressField getAddress();

    ClientProfile getClientProfile();

    FunctionCode getFunctionCode();

    Aleo getAleo();

    StructureCode getStructureCode();

    Channel getChannel();

    Cpa getCpa();

    Data getData();

    Crc getCrc();

}
