package com.elster.jupiter.appserver;

import java.util.List;

import com.elster.jupiter.messaging.SubscriberSpec;
import com.elster.jupiter.util.cron.CronExpression;

public interface AppServer {

    CronExpression getScheduleFrequency();

    String getName();

	List<SubscriberExecutionSpec> getSubscriberExecutionSpecs();

	SubscriberExecutionSpec createSubscriberExecutionSpec(SubscriberSpec subscriberSpec, int threadCount);

}
