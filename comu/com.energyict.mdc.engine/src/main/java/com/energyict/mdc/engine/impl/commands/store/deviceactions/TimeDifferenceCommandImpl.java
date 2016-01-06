package com.energyict.mdc.engine.impl.commands.store.deviceactions;

import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandType;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandTypes;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.collect.TimeDifferenceCommand;
import com.energyict.mdc.engine.impl.commands.store.core.SimpleComCommand;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.protocol.api.DeviceProtocol;

import java.text.MessageFormat;
import java.time.Clock;
import java.util.Date;
import java.util.Optional;

/**
 * Command which performs the action to collect and hold the TimeDifference of the device.
 *
 * @author gna
 * @since 10/05/12 - 9:48
 */
public class TimeDifferenceCommandImpl extends SimpleComCommand implements TimeDifferenceCommand {

    /**
     * The difference in time between the Collection Software and the Meter
     */
    private TimeDuration timeDifference;

    public TimeDifferenceCommandImpl(final CommandRoot commandRoot) {
        super(commandRoot);
    }

    @Override
    public void doExecute(final DeviceProtocol deviceProtocol, ExecutionContext executionContext) {
        Clock clock = getCommandRoot().getServiceProvider().clock();
        RoundTripTimer roundTripTimer = new RoundTripTimer(clock);
        roundTripTimer.start();
        Date meterTime = deviceProtocol.getTime();
        roundTripTimer.stop();
        long halfRoundTrip = roundTripTimer.getRoundTrip();
        halfRoundTrip = halfRoundTrip / 2;
        long differenceInMillis = clock.millis() - (meterTime.getTime() - halfRoundTrip);
        this.timeDifference = new TimeDuration((int) differenceInMillis, TimeDuration.TimeUnit.MILLISECONDS);
    }

    /**
     * @return the {@link #timeDifference}
     */
    public Optional<TimeDuration> getTimeDifference() {
        return Optional.ofNullable(timeDifference);
    }

    @Override
    public ComCommandType getCommandType() {
        return ComCommandTypes.TIME_DIFFERENCE_COMMAND;
    }

    @Override
    public String getDescriptionTitle() {
        return "Read out the device time difference";
    }

    @Override
    protected void toJournalMessageDescription(DescriptionBuilder builder, LogLevel serverLogLevel) {
        super.toJournalMessageDescription(builder, serverLogLevel);
        if (this.isJournalingLevelEnabled(serverLogLevel, LogLevel.INFO)) {
            builder.addLabel(MessageFormat.format("Time difference is {0}", this.timeDifference));
        }
    }

}
