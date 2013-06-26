package com.elster.jupiter.util.cron.impl;

import java.util.Date;

import com.elster.jupiter.util.cron.CronExpression;

class QuartzCronExpressionAdapter implements CronExpression {

	private final QuartzCronExpression quartzCronExpression;

    public QuartzCronExpressionAdapter(String expression) {
        this.quartzCronExpression = new QuartzCronExpression(expression);
    }

    @Override
    public Date nextAfter(Date date) {
        return quartzCronExpression.getNextValidTimeAfter(date);
    }

    @Override
    public boolean matches(Date date) {
        return quartzCronExpression.isSatisfiedBy(date);
    }
    
    @Override
	public String toString() {
		return quartzCronExpression.getCronExpression();
	}

}
