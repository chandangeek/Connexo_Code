package com.elster.utils.lis200.agrmodel;

import java.util.Comparator;

/**
 * Class for sorting data in an ArchiveData class
 *
 */
public class ArchiveLineComparatorByLineNumber
    implements Comparator<ArchiveLine> {
    /**
     * Comparator by line number
     *
     * @param l1 - ArchiveLine1
     * @param l2 - ArchiveLine2
     * @return int - result of comparison
     */
    public int compare(ArchiveLine l1, ArchiveLine l2) {

        if (l1.getLineNo() < l2.getLineNo()) {
            return -1;
        } else {
            if (l1.getLineNo() > l2.getLineNo()) {
                return 1;
            } else {
                return 0;
            }
        }
    }
}