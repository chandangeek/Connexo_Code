package com.elster.utils.lis200.agrmodel;

import java.util.Comparator;

/**
 * Class for sorting data in an ArchiveData class
 *
 */
public class ArchiveLineComparatorByDate
    implements Comparator<ArchiveLine> {
    /**
     * Comparator by date
     *
     * @param l1 - ArchiveLine1
     * @param o2 - ArchiveLine2
     * @return int - result of comparison
     */
    public int compare(ArchiveLine l1, ArchiveLine l2) {

        if (l1.getTimeStamp().getTime() < l2.getTimeStamp().getTime()) {
            return -1;
        } else {
            if (l1.getTimeStamp().getTime() > l2.getTimeStamp().getTime()) {
                return 1;
            } else {
                return 0;
            }
        }
    }
}