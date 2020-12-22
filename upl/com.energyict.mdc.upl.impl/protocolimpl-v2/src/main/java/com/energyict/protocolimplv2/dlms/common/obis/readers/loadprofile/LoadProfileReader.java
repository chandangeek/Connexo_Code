package com.energyict.protocolimplv2.dlms.common.obis.readers.loadprofile;

import com.energyict.mdc.upl.meterdata.CollectedLoadProfile;
import com.energyict.mdc.upl.meterdata.CollectedLoadProfileConfiguration;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.common.obis.ObisReader;

public interface LoadProfileReader  extends ObisReader<CollectedLoadProfile, com.energyict.protocol.LoadProfileReader, ObisCode> {

    CollectedLoadProfileConfiguration getChannelInfo(com.energyict.protocol.LoadProfileReader lpr, AbstractDlmsProtocol dlmsProtocol);

}
