package com.elster.utils.lis200.agrmodel;

import com.elster.agrimport.agrreader.AgrColumnHeader;

import java.util.List;

/**
 * class to check state columns ...
 * Per default, no state column, so state is taken from state of value (if possible).
 * <p/>
 * User: heuckeg
 * Date: 07.07.2010
 * Time: 13:41:38
 */
public class DefaultLineProcessor implements IArchiveLineProcessor {

    /**
     * Constructor
     *
     * @param columns
     */
    public DefaultLineProcessor(List<AgrColumnHeader> columns, int colIndex) {
    }

    /**
     * Get status of value
     *
     * @param line  - archive line
     * @param index - index of value in line
     * @return EIintervalstate
     */
    public int getIntervalState(ArchiveLine line, int index) {
        return 0;
    }
}
