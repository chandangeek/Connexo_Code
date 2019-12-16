package com.energyict.mdc.engine.offline.gui.models;

import com.elster.jupiter.time.RelativeDate;
import com.elster.jupiter.time.RelativeOperation;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * DefaultListModel representing the List of RelativeDateExpressionParts of a RelativeDate.
 */
public class RelativeOperationListModel extends DefaultListModel {

    private boolean emptyAsNull = false;

    public RelativeOperationListModel() {
    }

    /**
     * Creates a new instance of the model using the given RelativeDate
     *
     * @param relativeDate to build the model
     */
    public RelativeOperationListModel(RelativeDate relativeDate) {
        if (relativeDate != null){
            for (RelativeOperation part : relativeDate.getOperations()) {
                addElement(part);
            }
        }
    }

    /**
     * @param flag when set to true the getRelativeDate method will return null if the model is empty
     *             otherwise it will return a non initialized RelativePeriod (called 'Custom')
     */
    public void setEmptyAsNull(boolean flag) {
        emptyAsNull = flag;
    }

    public RelativeDate getRelativeDate() {
        if (isEmpty() && emptyAsNull) {
            return null;
        } else {
            return new RelativeDate(getRelativeOperations());
        }
    }

    public String toString(){
        return new RelativeDate(getRelativeOperations()).getRelativeDate();
    }

    public boolean isValid(){
//        for (RelativeOperation part: getRelativeOperations()){
//             if (!part.getField().isValid()){
//                 return false;
//             }
//        }
        return true;
    }

    public boolean includesWorkDayPart(){
//        for (RelativeOperation part: getRelativeOperations()){
//             if (RelativeOperation.Field.WORK_DAY.equals(part.getField())){
//                 return true;
//             }
//        }
        return false;
    }

    private List<RelativeOperation> getRelativeOperations(){
        final List<RelativeOperation> parts = new ArrayList<RelativeOperation>();
        Enumeration elements = elements();
        while (elements.hasMoreElements()) {
            RelativeOperation part = (RelativeOperation) elements.nextElement();
            parts.add(part);
        }
        return parts;
    }
}
