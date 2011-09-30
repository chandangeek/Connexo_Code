package com.elster.utils.lis200.agrmodel;

/**
 * Class for sorting data in an ArchiveData class
 *
 */
public class ArchiveLineComaratorByGlobalSequenceNo {
    /**
     * Comparator by sequence number
     *
     * @param l1 - LineInfo1
     * @param l2 - LineInfo2
     * @return int - result of comparison
     */
    public static int compareBySequence(ArchiveLine l1, ArchiveLine l2) {


        if (l1.getGlobalSeqNo() < l2.getGlobalSeqNo()) {
            return -1;
        } else {
            if (l1.getGlobalSeqNo() > l2.getGlobalSeqNo()) {
                return 1;
            } else {
                return 0;
            }
        }
    }
}