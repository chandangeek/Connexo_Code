package com.elster.utils.lis200.agrmodel;


import com.elster.agrimport.agrreader.AgrColumnHeader;
import com.elster.agrimport.agrreader.IAgrValue;
import com.elster.agrimport.agrreader.IStatedAgrValue;

import java.util.List;

/**
 * class to check state columns ...
 * Per default, no state column, so state is taken from state of value (if possible).
 * <p/>
 * User: heuckeg
 * Date: 07.07.2010
 * Time: 13:52:08
 */
public class DsfgLineProcessor implements IArchiveLineProcessor {
    /**
     * Constructor
     *
     * @param columns - header info
     * @param colIndex - index of value in line
     */
    public DsfgLineProcessor(List<AgrColumnHeader> columns, int colIndex) {
    }

    /**
     * Get status of value
     *
     * @param line - archive line
     * @param index - index of value in line
     * @return EIintervalstate
     */
    public int getIntervalState(ArchiveLine line, int index) {
        IAgrValue v = line.getValue(index);
        if (v instanceof IStatedAgrValue) {
            return ((IStatedAgrValue) v).getStatus();
        } else {
            return 0;
        }
    }
}
