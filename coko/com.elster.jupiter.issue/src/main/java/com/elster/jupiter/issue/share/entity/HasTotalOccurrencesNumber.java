package com.elster.jupiter.issue.share.entity;

/**
 *  This interface is design to extend {@link Issue} interface functionallity.
 *
 *  When we need to add aditional logic to {@link Issue} objects, it is nice to have
 *  segregated interfaces that are located in {@link com.elster.jupiter.issue} package.
 *
 *  Any {@link Issue} that extends this interface must return total amount of occurrences that exist in the system
 *  on the moment of issue creation. By occurrence we mean an event that triggered following issue creation process.
 *
 * @author edragutan
 */
public interface HasTotalOccurrencesNumber {

    /**
     * Use this setter to set total occurrences number for an issue
     * @param totalOccurrencesNumber
     */
    void setTotalOccurrencesNumber(long totalOccurrencesNumber);

    /**
     * Use this getter to get total number of occurrences for an issue at the moment of issue creation
     * @return total number of occurrences for an issue
     */
    long getTotalOccurrencesNumber();

}
