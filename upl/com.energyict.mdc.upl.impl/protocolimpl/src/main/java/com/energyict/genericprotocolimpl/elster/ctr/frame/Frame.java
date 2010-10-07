package com.energyict.genericprotocolimpl.elster.ctr.frame;

import com.energyict.genericprotocolimpl.elster.ctr.common.Field;
import com.energyict.genericprotocolimpl.elster.ctr.frame.field.*;

/**
 * Copyrights EnergyICT
 * Date: 27-sep-2010
 * Time: 8:32:29
 */
public interface Frame<T extends Frame> extends Field<T> {

    public Address getAddress();

    public void setAddress(Address address);

    public Channel getChannel();

    public void setChannel(Channel channel);

    public Cpa getCpa();

    public void setCpa(Cpa cpa);

    public Crc getCrc();

    public void setCrc(Crc crc);

    public void setCrc();

    public Data getData();

    public void setData(Data data);

    public FunctionCode getFunctionCode();

    public void setFunctionCode(FunctionCode functionCode);

    public Profi getProfi();

    public void setProfi(Profi profi);

    public StructureCode getStructureCode();

    public void setStructureCode(StructureCode structureCode);

}
