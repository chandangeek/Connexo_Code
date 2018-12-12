package com.elster.protocolimpl.lis200.objects;

import com.elster.protocolimpl.lis200.utils.RawArchiveLineInfo;
import com.energyict.protocolimpl.iec1107.ProtocolLink;

/**
 * User: heuckeg
 * Date: 07.04.11
 * Time: 15:25
 */
@SuppressWarnings({"unused"})
public class HistoricalValueObject extends SimpleObject  {

    private final RawArchiveLineInfo ralInfo;

    public HistoricalValueObject(ProtocolLink link, int instance, RawArchiveLineInfo archiveLineInfo) {
        super(link, instance, "");
        ralInfo = archiveLineInfo;
    }

    public RawArchiveLineInfo getArchiveLineInfo() {
        return ralInfo;
    }

    public int getArchiveInstance() {
        return this.getObjectInstance();
    }
}
