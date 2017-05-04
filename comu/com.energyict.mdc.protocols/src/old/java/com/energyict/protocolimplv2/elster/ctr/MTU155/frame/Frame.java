/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.ctr.MTU155.frame;

import com.energyict.protocolimplv2.elster.ctr.MTU155.common.Field;
import com.energyict.protocolimplv2.elster.ctr.MTU155.frame.field.Address;
import com.energyict.protocolimplv2.elster.ctr.MTU155.frame.field.Channel;
import com.energyict.protocolimplv2.elster.ctr.MTU155.frame.field.Cpa;
import com.energyict.protocolimplv2.elster.ctr.MTU155.frame.field.Crc;
import com.energyict.protocolimplv2.elster.ctr.MTU155.frame.field.Data;
import com.energyict.protocolimplv2.elster.ctr.MTU155.frame.field.FunctionCode;
import com.energyict.protocolimplv2.elster.ctr.MTU155.frame.field.Profi;
import com.energyict.protocolimplv2.elster.ctr.MTU155.frame.field.StructureCode;

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
