package com.elster.utils.lis200.agrmodel;

import java.util.Comparator;

/**
 * Class for sorting data in an ArchiveData class
 *
 * User: heuckeg
 * Date: 08.07.2010
 * Time: 14:08:49
 */
public class ArchiveLineComparatorByDateDesc
    implements Comparator<ArchiveLine> {
    /**
     * Comparator by date
     *
     * @param l1 - ArchiveLine1
     * @param l2 - ArchiveLine2
     * @return int - result of comparison
     */
    public int compare(ArchiveLine l1, ArchiveLine l2) {

        return l1.getTimeStamp().compareTo(l2.getTimeStamp());
/*
        if (l2.getTimeStamp().getTime() < l1.getTimeStamp().getTime()) {
            return -1;
        } else {
            if (l2.getTimeStamp().getTime() > l1.getTimeStamp().getTime()) {
                return 1;
            } else {
                return 0;
            }
        }
*/
    }
}
