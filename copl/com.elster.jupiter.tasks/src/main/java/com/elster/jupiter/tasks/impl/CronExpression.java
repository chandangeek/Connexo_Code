package com.elster.jupiter.tasks.impl;

import java.util.Date;

interface CronExpression {

    Date nextAfter(Date date);

    boolean matches(Date date);
}
