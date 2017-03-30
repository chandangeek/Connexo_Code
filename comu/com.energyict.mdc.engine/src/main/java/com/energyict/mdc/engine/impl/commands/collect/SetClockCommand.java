/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.collect;

/**
 * Command to set the device time on the current system time <b>if and only if</b> the timeDifference is between
 * the Minimum and Maximum defined times. Otherwise a warning will be added to the issueList.
 *
 * @author gna
 * @since 29/05/12 - 10:01
 */
public interface SetClockCommand extends ComCommand {

}
