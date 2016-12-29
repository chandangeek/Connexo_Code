package com.energyict.mdc.device.command.impl.exceptions;

public class ExceededCommandRule {
    private String name;
    private boolean dayLimitExceeded;
    private boolean weekLimitExceeded;
    private boolean monthLimitExceeded;

    public ExceededCommandRule(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public boolean isDayLimitExceeded() {
        return dayLimitExceeded;
    }

    public void setDayLimitExceeded(boolean dayLimitExceeded) {
        this.dayLimitExceeded = dayLimitExceeded;
    }

    public boolean isWeekLimitExceeded() {
        return weekLimitExceeded;
    }

    public void setWeekLimitExceeded(boolean weekLimitExceeded) {
        this.weekLimitExceeded = weekLimitExceeded;
    }

    public boolean isMonthLimitExceeded() {
        return monthLimitExceeded;
    }

    public void setMonthLimitExceeded(boolean monthLimitExceeded) {
        this.monthLimitExceeded = monthLimitExceeded;
    }

    public boolean isLimitExceeded() {
        return isDayLimitExceeded() || isWeekLimitExceeded() || isMonthLimitExceeded();
    }
}
