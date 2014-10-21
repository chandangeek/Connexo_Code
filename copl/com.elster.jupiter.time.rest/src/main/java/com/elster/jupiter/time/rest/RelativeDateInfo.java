package com.elster.jupiter.time.rest;

import com.elster.jupiter.time.RelativeDate;
import com.elster.jupiter.time.RelativeField;
import com.elster.jupiter.time.RelativeOperation;
import com.elster.jupiter.time.RelativeOperator;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import java.time.DayOfWeek;
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
    @JsonProperty("ampm")
    public Long ampm;

    public RelativeDateInfo() {}

    public RelativeDateInfo(RelativeDate relativeDate) {
        List<RelativeOperation> relativeOperations = relativeDate.getOperations();
        relativeOperations.stream().forEach(ro -> {
            RelativeField relativeField = ro.getField();
            switch (relativeField) {
                case MONTH:
                case WEEK:
                case HOUR:
                case MINUTES:
                    startPeriodAgo = relativeField.getChronoUnit().toString();
                    startAmountAgo = ro.getShift();
                    break;
                case DAY_IN_MONTH:
                    startFixedDay = ro.getShift();
                    onDayOfMonth = ro.getShift();
                    break;
                case MONTH_IN_YEAR:
                    startFixedMonth = ro.getShift();
                    break;
                case YEAR:
                    startFixedYear = ro.getShift();
                    break;
                case START_NOW:
                    startNow = true;
                    break;
                case CURRENT_DAY_OF_MONTH:
                    onCurrentDay = true;
                    break;
                case DAY_IN_WEEK:
                    onDayOfWeek = ro.getShift();
                    break;
                case HOUR_OF_DAY:
                    atHour = ro.getShift();
                    break;
                case MINUTES_OF_HOUR:
                    atMinute = ro.getShift();
                    break;
                case AMPM_OF_DAY:
                    ampm = ro.getShift();
                    break;
            }
        });

    }

    public List<RelativeOperation> convertToRelativeOperations() {
        List<RelativeOperation> operations = new ArrayList<>();
        if(startPeriodAgo != null && startAmountAgo != null) {
            switch (startPeriodAgo) {
                case "months":
                    operations.add(new RelativeOperation(RelativeField.MONTH, RelativeOperator.MINUS, startAmountAgo));
                    break;
                case "weeks":
                    operations.add(new RelativeOperation(RelativeField.WEEK, RelativeOperator.MINUS, startAmountAgo));
                    break;
                case "days":
                    operations.add(new RelativeOperation(RelativeField.DAY, RelativeOperator.MINUS, startAmountAgo));
                    break;
                case "hours":
                    operations.add(new RelativeOperation(RelativeField.HOUR, RelativeOperator.MINUS, startAmountAgo));
                    break;
                case "minutes":
                    operations.add(new RelativeOperation(RelativeField.MINUTES, RelativeOperator.MINUS, startAmountAgo));
                    break;
            }
        }
        if (startFixedDay!= null && startFixedMonth!= null && startFixedYear !=null) {
            operations.add(new RelativeOperation(RelativeField.DAY_IN_MONTH, RelativeOperator.EQUAL, startFixedDay));
            operations.add(new RelativeOperation(RelativeField.MONTH_IN_YEAR, RelativeOperator.EQUAL, startFixedMonth));
            operations.add(new RelativeOperation(RelativeField.YEAR, RelativeOperator.EQUAL, startFixedYear));
        } else if (onDayOfMonth != null) {
            operations.add(new RelativeOperation(RelativeField.DAY_IN_MONTH, RelativeOperator.EQUAL, startFixedDay));
        }
        if (onCurrentDay != null) {
            operations.add(new RelativeOperation(RelativeField.CURRENT_DAY_OF_MONTH, RelativeOperator.EQUAL, 1));
        }
        if(onDayOfWeek != null) {
            operations.add(new RelativeOperation(RelativeField.DAY_IN_WEEK, RelativeOperator.EQUAL, onDayOfWeek));
        }
        if(atHour != null && ampm != null) {
            operations.add(new RelativeOperation(RelativeField.HOUR_OF_DAY, RelativeOperator.EQUAL, atHour));
            operations.add(new RelativeOperation(RelativeField.AMPM_OF_DAY, RelativeOperator.EQUAL, ampm));
        }
        if(atMinute != null) {
            operations.add(new RelativeOperation(RelativeField.MINUTES_OF_HOUR, RelativeOperator.EQUAL, atMinute));
        }
        if(startNow != null) {
            operations.add(new RelativeOperation(RelativeField.START_NOW, RelativeOperator.EQUAL, 1));
        }
        return operations;
    }
}
