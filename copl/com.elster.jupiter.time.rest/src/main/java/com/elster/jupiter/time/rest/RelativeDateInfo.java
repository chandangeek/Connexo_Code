package com.elster.jupiter.time.rest;

import com.elster.jupiter.time.RelativeDate;
import com.elster.jupiter.time.RelativeField;
import com.elster.jupiter.time.RelativeOperation;
import com.elster.jupiter.time.RelativeOperator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class RelativeDateInfo {
    @JsonProperty("startAmountAgo")
    public Long startAmountAgo;
    @JsonProperty("startPeriodAgo")
    public String startPeriodAgo;
    @JsonProperty("startFixedDay")
    public Long startFixedDay;
    @JsonProperty("startFixedMonth")
    public Long startFixedMonth;
    @JsonProperty("startFixedYear")
    public Long startFixedYear;
    @JsonProperty("startNow")
    public Boolean startNow;
    @JsonProperty("onCurrentDay")
    public Boolean onCurrentDay;
    @JsonProperty("onDayOfMonth")
    public Long onDayOfMonth;
    @JsonProperty("onDayOfWeek")
    public Long onDayOfWeek;
    @JsonProperty("atHour")
    public Long atHour;
    @JsonProperty("atMinute")
    public Long atMinute;

    public RelativeDateInfo() {}

    public RelativeDateInfo(RelativeDate relativeDate) {
        List<RelativeOperation> relativeOperations = relativeDate.getOperations();
        relativeOperations.stream().forEach(ro -> {
            RelativeField relativeField = ro.getField();
            switch (relativeField) {
                case YEAR:
                    startFixedYear = ro.getShift();
                    break;
                case MONTH:
                    if(ro.getOperator().equals(RelativeOperator.PLUS) || ro.getOperator().equals(RelativeOperator.MINUS)) {
                        startPeriodAgo = relativeField.getChronoUnit().toString().toLowerCase();
                        startAmountAgo = ro.getShift();
                    } else {
                        startFixedMonth = ro.getShift();
                    }
                    break;
                case WEEK:
                    startPeriodAgo = relativeField.getChronoUnit().toString().toLowerCase();
                    startAmountAgo = ro.getShift();
                    break;
                case DAY_OF_WEEK:
                    onDayOfWeek = ro.getShift();
                    break;
                case DAY:
                    if(ro.getOperator().equals(RelativeOperator.PLUS) || ro.getOperator().equals(RelativeOperator.MINUS)) {
                        startPeriodAgo = relativeField.getChronoUnit().toString().toLowerCase();
                        startAmountAgo = ro.getShift();
                    } else {
                        onDayOfMonth = ro.getShift();
                        startFixedDay = ro.getShift();
                    }
                    break;
                case HOUR:
                    if(ro.getOperator().equals(RelativeOperator.PLUS) || ro.getOperator().equals(RelativeOperator.MINUS)) {
                        startPeriodAgo = relativeField.getChronoUnit().toString().toLowerCase();
                        startAmountAgo = ro.getShift();
                    } else {
                        atHour = ro.getShift();
                    }
                    break;
                case MINUTES:
                    if(ro.getOperator().equals(RelativeOperator.PLUS) || ro.getOperator().equals(RelativeOperator.MINUS)) {
                        startPeriodAgo = relativeField.getChronoUnit().toString().toLowerCase();
                        startAmountAgo = ro.getShift();
                    } else {
                        atMinute = ro.getShift();
                    }
                    break;
            }
        });
        if (startPeriodAgo != null && startPeriodAgo.equals(RelativeField.MONTH.getChronoUnit().toString().toLowerCase()) && (onDayOfMonth == null && startFixedDay == null)) {
            onCurrentDay = true;
        }
        if (startPeriodAgo == null && startFixedYear == null) {
            startNow = true;
        }
    }

    public List<RelativeOperation> convertToRelativeOperations() {
        List<RelativeOperation> operations = new ArrayList<>();
        boolean dayOfWeekEnabled = true;
        boolean dayOfMonthEnabled = true;
        boolean hourEnabled = true;
        boolean minuteEnabled = true;
        if(startPeriodAgo != null && startAmountAgo != null) {
            switch (startPeriodAgo) {
                case "months":
                    operations.add(new RelativeOperation(RelativeField.MONTH, RelativeOperator.MINUS, startAmountAgo));
                    dayOfWeekEnabled = false;
                    break;
                case "weeks":
                    operations.add(new RelativeOperation(RelativeField.WEEK, RelativeOperator.MINUS, startAmountAgo));
                    dayOfMonthEnabled = false;
                    break;
                case "days":
                    operations.add(new RelativeOperation(RelativeField.DAY, RelativeOperator.MINUS, startAmountAgo));
                    dayOfWeekEnabled = false;
                    dayOfMonthEnabled = false;
                    break;
                case "hours":
                    operations.add(new RelativeOperation(RelativeField.HOUR, RelativeOperator.MINUS, startAmountAgo));
                    dayOfWeekEnabled = false;
                    dayOfMonthEnabled = false;
                    hourEnabled = false;
                    break;
                case "minutes":
                    operations.add(new RelativeOperation(RelativeField.MINUTES, RelativeOperator.MINUS, startAmountAgo));
                    dayOfWeekEnabled = false;
                    dayOfMonthEnabled = false;
                    hourEnabled = false;
                    minuteEnabled = false;
                    break;
            }
        }
        if (startFixedDay!= null && startFixedMonth!= null && startFixedYear !=null) {
            operations.add(new RelativeOperation(RelativeField.DAY, RelativeOperator.EQUAL, startFixedDay));
            operations.add(new RelativeOperation(RelativeField.MONTH, RelativeOperator.EQUAL, startFixedMonth));
            operations.add(new RelativeOperation(RelativeField.YEAR, RelativeOperator.EQUAL, startFixedYear));
        } else if (onDayOfMonth != null && dayOfMonthEnabled) {
            operations.add(new RelativeOperation(RelativeField.DAY, RelativeOperator.EQUAL, onDayOfMonth));
        }
        if(onDayOfWeek != null && dayOfWeekEnabled) {
            operations.add(new RelativeOperation(RelativeField.DAY_OF_WEEK, RelativeOperator.EQUAL, onDayOfWeek));
        }
        if(atHour != null && hourEnabled) {
            operations.add(new RelativeOperation(RelativeField.HOUR, RelativeOperator.EQUAL, atHour));
        }
        if(atMinute != null && minuteEnabled) {
            operations.add(new RelativeOperation(RelativeField.MINUTES, RelativeOperator.EQUAL, atMinute));
        }
        return operations;
    }
}
