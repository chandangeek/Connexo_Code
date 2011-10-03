package com.elster.utils.lis200.agrmodel;

/**
 * Class for sorting data in an ArchiveData class
 *
 */
import java.util.Comparator;

public class ArchiveLineComparatorByLineNoDesc
        implements Comparator<ArchiveLine> {

    /**
     * Comparator by line number
     *
     * @param o1 - LineInfo1
     * @param o2 - LineInfo2
     * @return int - result of comparison
     */

    public int compare(ArchiveLine o1, ArchiveLine o2) {

        if (o2.getLineNo() < o1.getLineNo()) {
            return -1;
        } else {
            if (o2.getLineNo() > o1.getLineNo()) {
                return 1;
            } else {
                return 0;
            }
        }
    }
}