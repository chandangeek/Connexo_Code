/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.tasks;

import com.energyict.mdc.common.comserver.ComServer;

import aQute.bnd.annotation.ConsumerType;

/**
 * A PriorityComTaskExecution is a special variant of the regular {@link ComTaskExecution}
 * which is intended to be executed with <b>high priority</b> when picked up and executed by any {@link ComServer}.
 * Moreover, the {@link ComServer} which picks up the task is allowed to interrupt other tasks in order to execute this one.
 * <p>
 * Before, a user needed to set the next execution timestamp of a ComTaskExecution somewhere in the past (the further
 * the higher the priority) to get this task at the front of the task queue but then when the task failed it would simply
 * be rescheduled (typically the next five minutes) and as a result end up in the bulk of all other taks that are
 * scheduled. The high priority aspect of the task is lost.
 * <p>
 * {@link PriorityComTaskExecution}s are prioritized in the same way as ComTaskExecutions and by default, the priority of
 * the wrapped ComTaskExecution is copied.
 * <p>
 * As soon as the {@link PriorityComTaskExecution} is completed, it will disappear from the system, copying failure/success
 * status information to the actual ComTaskExecution. Moreover the actual ComTaskExecution will be rescheduled with normal priority.
 * <p>
 * <b>Remark:</b> HighPriorityComTaskExecution objects are not persisted 1-on-1 to the database, but instead in the background a
 * {@link PriorityComTaskExecutionLink} object will be constructed and persisted to the database.
 *
 * @author sva
 * @since 29/04/2016 - 10:59
 */
@ConsumerType
public interface PriorityComTaskExecution extends ServerComTaskExecution {

}
