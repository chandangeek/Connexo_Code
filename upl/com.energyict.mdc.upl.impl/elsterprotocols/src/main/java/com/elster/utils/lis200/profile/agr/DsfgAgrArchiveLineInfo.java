package com.elster.utils.lis200.profile.agr;

import com.elster.agrimport.agrreader.AgrColumnHeader;
import com.energyict.protocol.IntervalStateBits;

import java.util.List;

/**
 * User: heuckeg
 * Date: 09.07.2010
 * Time: 13:56:36
 */
public class DsfgAgrArchiveLineInfo extends AgrArchiveLineInfo {

    public DsfgAgrArchiveLineInfo(List<AgrColumnHeader> headerInfo) {
        super(headerInfo);

        hasValueStateCols = isHavingOnlyStatedValues();
    }

    public int translateValueStateToEIState(int state) {
        return (state != 0) ? IntervalStateBits.CORRUPTED : 0;
    }
}
