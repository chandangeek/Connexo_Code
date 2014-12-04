package com.energyict.protocolimplv2.elster.ctr.MTU155.frame;

import com.energyict.protocolimplv2.elster.ctr.MTU155.common.Field;
import com.energyict.protocolimplv2.elster.ctr.MTU155.frame.field.*;

/**
 * Copyrights EnergyICT
 * Date: 27-sep-2010
 * Time: 8:32:29
 */
public interface Frame<T extends Frame> extends Field<T> {

    Address getAddress();

    void setAddress(Address address);

    Channel getChannel();

    void setChannel(Channel channel);

    Cpa getCpa();

    void setCpa(Cpa cpa);

    Crc getCrc();

    void setCrc(Crc crc);

    void setCrc();

    Data getData();

    void setData(Data data);

    FunctionCode getFunctionCode();

    void setFunctionCode(FunctionCode functionCode);

    Profi getProfi();

    void setProfi(Profi profi);

    StructureCode getStructureCode();

    void setStructureCode(StructureCode structureCode);

    void generateAndSetCpa(byte[] key);

}
