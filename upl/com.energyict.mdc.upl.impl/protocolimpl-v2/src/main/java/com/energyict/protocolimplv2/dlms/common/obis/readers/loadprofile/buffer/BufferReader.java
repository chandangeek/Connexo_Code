package com.energyict.protocolimplv2.dlms.common.obis.readers.loadprofile.buffer;

import com.energyict.dlms.DataContainer;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;

import java.io.IOException;

public interface BufferReader {
    DataContainer read(AbstractDlmsProtocol protocol, ObisCode deviceLoadProfileObisCode, LoadProfileReader loadProfileReader) throws IOException;
}
