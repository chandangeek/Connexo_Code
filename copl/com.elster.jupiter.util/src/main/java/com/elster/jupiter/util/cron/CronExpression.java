package com.elster.jupiter.util.cron;

import java.util.Date;

public interface CronExpression {

    Date nextAfter(Date date);

    boolean matches(Date date);
}
