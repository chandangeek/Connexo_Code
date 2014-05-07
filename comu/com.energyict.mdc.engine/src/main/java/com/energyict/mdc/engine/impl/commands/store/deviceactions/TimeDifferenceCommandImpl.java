package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.elster.jupiter.util.time.Clock;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.collect.TimeDifferenceCommand;
import com.energyict.mdc.engine.impl.commands.store.core.SimpleComCommand;
import com.energyict.mdc.engine.impl.core.JobExecution;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import java.text.MessageFormat;
import java.util.Date;

/**
 * Command which performs the action to collect and hold the TimeDifference of the device.
 *
 * @author gna
 * @since 10/05/12 - 9:48
 */
public class TimeDifferenceCommandImpl extends SimpleComCommand implements TimeDifferenceCommand {

    private final Clock clock;
    /**
     * The difference in time between the Collection Software and the Meter
     */
    private TimeDuration timeDifference;

    public TimeDifferenceCommandImpl(final CommandRoot commandRoot, Clock clock) {
        super(commandRoot);
        this.clock = clock;
    }

    @Override
    public void doExecute (final DeviceProtocol deviceProtocol, JobExecution.ExecutionContext executionContext) {
        RoundTripTimer roundTripTimer = new RoundTripTimer(clock);
        roundTripTimer.start();
        Date meterTime = deviceProtocol.getTime();
        roundTripTimer.stop();
        long halfRoundTrip = roundTripTimer.getRoundTrip();
        if (halfRoundTrip != 0) {
            halfRoundTrip = halfRoundTrip / 2;
        }
        long differenceInMillis = this.clock.now().getTime() - (meterTime.getTime() - halfRoundTrip);
        this.timeDifference = new TimeDuration((int) differenceInMillis, TimeDuration.MILLISECONDS);
    }

    /**
     * @return the {@link #timeDifference}
     */
    public TimeDuration getTimeDifference() {
        if(this.timeDifference == null){
            return TimeDifferenceCommand.DID_NOT_READ_TIME_DIFFERENCE;
        }
        return timeDifference;
    }

    @Override
    public ComCommandTypes getCommandType() {
        return ComCommandTypes.TIME_DIFFERENCE_COMMAND;
    }

    @Override
    protected void toJournalMessageDescription (DescriptionBuilder builder, LogLevel serverLogLevel) {
        super.toJournalMessageDescription(builder, serverLogLevel);
        if (this.isJournalingLevelEnabled(serverLogLevel, LogLevel.INFO)) {
            builder.addLabel(MessageFormat.format("Time difference is {0}", this.timeDifference));
        }
    }

}
