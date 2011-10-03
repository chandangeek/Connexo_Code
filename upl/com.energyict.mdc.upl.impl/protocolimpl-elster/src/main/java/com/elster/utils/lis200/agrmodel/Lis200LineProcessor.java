package com.elster.utils.lis200.agrmodel;

import com.elster.agrimport.agrreader.AgrColumnHeader;

import java.util.List;

/**
 * User: heuckeg
 * Date: 07.07.2010
 * Time: 14:03:07
 */
public class Lis200LineProcessor implements IArchiveLineProcessor {

    public Lis200LineProcessor(List<AgrColumnHeader> columns, int colIndex) {
    }

    /* TODO: interval state of lis200 devs */
    public int getIntervalState(ArchiveLine line, int index) {
        return 0;
    }
}
