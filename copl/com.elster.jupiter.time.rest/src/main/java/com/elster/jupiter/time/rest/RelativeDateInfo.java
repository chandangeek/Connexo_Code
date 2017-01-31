/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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
    @JsonProperty("startTimeMode")
    public String startTimeMode;
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
        this();
        relativeDate.getOperations().forEach(this::setOptions);
        if (startPeriodAgo != null && startPeriodAgo.equals(RelativeField.MONTH.getChronoUnit().toString().toLowerCase()) && (onDayOfMonth == null && startFixedDay == null)) {
            onCurrentDay = true;
        }
        if (startPeriodAgo == null && startFixedYear == null) {
            startNow = true;
        }
    }

    private void setOptions(RelativeOperation relativeOperation) {
        RelativeField relativeField = relativeOperation.getField();
        RelativeOperator relativeOperator = relativeOperation.getOperator();
        switch (relativeField) {
            case YEAR:
                if (relativeOperator.equals(RelativeOperator.PLUS) || relativeOperator.equals(RelativeOperator.MINUS)) {
                    setAgoOptions(relativeOperation);
                } else {
                    startFixedYear = relativeOperation.getShift();
                }
                break;
            case MONTH:
                if (relativeOperator.equals(RelativeOperator.PLUS) || relativeOperator.equals(RelativeOperator.MINUS)) {
                    setAgoOptions(relativeOperation);
                } else {
                    startFixedMonth = relativeOperation.getShift();
                    if (RelativeField.MONTH.getChronoUnit().toString().toLowerCase().equals(startPeriodAgo)) {
                        this.clearAgoOptions();
                    }
                }
                break;
            case WEEK:
                setAgoOptions(relativeOperation);
                break;
            case DAY_OF_WEEK:
                onDayOfWeek = relativeOperation.getShift();
                break;
            case DAY:
                if (relativeOperator.equals(RelativeOperator.PLUS) || relativeOperator.equals(RelativeOperator.MINUS)) {
                    setAgoOptions(relativeOperation);
                } else {
                    onDayOfMonth = relativeOperation.getShift();
                    startFixedDay = relativeOperation.getShift();
                }
                break;
            case HOUR:
                if (relativeOperator.equals(RelativeOperator.PLUS) || relativeOperator.equals(RelativeOperator.MINUS)) {
                    setAgoOptions(relativeOperation);
                } else {
                    atHour = relativeOperation.getShift();
                    if (RelativeField.HOUR.getChronoUnit().toString().toLowerCase().equals(startPeriodAgo)) {
                        this.clearAgoOptions();
                    }
                }
                break;
            case MINUTES:
                if (relativeOperator.equals(RelativeOperator.PLUS) || relativeOperator.equals(RelativeOperator.MINUS)) {
                    setAgoOptions(relativeOperation);
                } else {
                    atMinute = relativeOperation.getShift();
                }
                break;
        }
    }

    public List<RelativeOperation> convertToRelativeOperations() {
        List<RelativeOperation> operations = new ArrayList<>();
        boolean dayOfWeekEnabled = true;
        boolean dayOfMonthEnabled = true;
        boolean hourEnabled = true;
        boolean minuteEnabled = true;
        if (startPeriodAgo != null && startAmountAgo != null && startTimeMode != null) {
            RelativeOperator operator = "ago".equals(startTimeMode) ? RelativeOperator.MINUS : RelativeOperator.PLUS;
            switch (startPeriodAgo) {
                case "years":
                    operations.add(new RelativeOperation(RelativeField.YEAR, operator, startAmountAgo));
                    dayOfWeekEnabled = false;
                    break;
                case "months":
                    operations.add(new RelativeOperation(RelativeField.MONTH, operator, startAmountAgo));
                    dayOfWeekEnabled = false;
                    break;
                case "weeks":
                    operations.add(new RelativeOperation(RelativeField.WEEK, operator, startAmountAgo));
                    dayOfMonthEnabled = false;
                    break;
                case "days":
                    operations.add(new RelativeOperation(RelativeField.DAY, operator, startAmountAgo));
                    dayOfWeekEnabled = false;
                    dayOfMonthEnabled = false;
                    break;
                case "hours":
                    operations.add(new RelativeOperation(RelativeField.HOUR, operator, startAmountAgo));
                    dayOfWeekEnabled = false;
                    dayOfMonthEnabled = false;
                    hourEnabled = false;
                    break;
                case "minutes":
                    operations.add(new RelativeOperation(RelativeField.MINUTES, operator, startAmountAgo));
                    dayOfWeekEnabled = false;
                    dayOfMonthEnabled = false;
                    hourEnabled = false;
                    minuteEnabled = false;
                    break;
            }
        } else if (startNow != null && startNow) {
            dayOfWeekEnabled = false;
            hourEnabled = false;
            minuteEnabled = false;
            dayOfMonthEnabled = false;
        }
        if (startFixedDay!= null && startFixedMonth!= null && startFixedYear !=null) {
            operations.add(new RelativeOperation(RelativeField.DAY, RelativeOperator.EQUAL, startFixedDay));
            operations.add(new RelativeOperation(RelativeField.MONTH, RelativeOperator.EQUAL, startFixedMonth));
            operations.add(new RelativeOperation(RelativeField.YEAR, RelativeOperator.EQUAL, startFixedYear));
            dayOfWeekEnabled = false;
            dayOfMonthEnabled = false;
        } else if (onDayOfMonth != null && dayOfMonthEnabled) {
            operations.add(new RelativeOperation(RelativeField.DAY, RelativeOperator.EQUAL, onDayOfMonth));
        }
        if (onDayOfWeek != null && dayOfWeekEnabled) {
            operations.add(new RelativeOperation(RelativeField.DAY_OF_WEEK, RelativeOperator.EQUAL, onDayOfWeek));
        }
        if (atHour != null && hourEnabled) {
            operations.add(new RelativeOperation(RelativeField.HOUR, RelativeOperator.EQUAL, atHour));
        }
        if (atMinute != null && minuteEnabled) {
            operations.add(new RelativeOperation(RelativeField.MINUTES, RelativeOperator.EQUAL, atMinute));
        }
        return operations;
    }

    private void setAgoOptions(RelativeOperation relativeOperation) {
        startPeriodAgo = relativeOperation.getField().getChronoUnit().toString().toLowerCase();
        startAmountAgo = relativeOperation.getShift();
        startTimeMode = relativeOperation.getOperator().equals(RelativeOperator.PLUS) ? "ahead" : "ago";
    }

    private void clearAgoOptions() {
        startPeriodAgo = null;
        startAmountAgo = null;
        startTimeMode = null;
    }

}