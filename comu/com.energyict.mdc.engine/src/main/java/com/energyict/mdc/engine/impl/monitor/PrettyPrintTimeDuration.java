package com.energyict.mdc.engine.impl.monitor;

import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.engine.exceptions.MessageSeeds;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.MessageSeed;
import org.joda.time.DateTimeConstants;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Data holder for a relative period in time
 * that pretty prints itself unlike the {@link TimeDuration}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-04-03 (14:57)
 */
public class PrettyPrintTimeDuration {

    private static final int DAYS_IN_MONTH = 30;
    private static final int SECONDS_IN_MONTH = DateTimeConstants.SECONDS_PER_HOUR * DateTimeConstants.HOURS_PER_DAY * DAYS_IN_MONTH;
    private static final int DAYS_IN_YEAR = 365;
    private static final int SECONDS_IN_YEAR = DateTimeConstants.SECONDS_PER_HOUR * DateTimeConstants.HOURS_PER_DAY * DAYS_IN_YEAR;

    private final Thesaurus thesaurus;
    private int seconds;
    private int minutes;
    private int hours;
    private int days;
    private int months;
    private int years;

    public PrettyPrintTimeDuration (TimeDuration timeDuration, Thesaurus thesaurus) {
        super();
        this.thesaurus = thesaurus;
        this.seconds = timeDuration.getSeconds();
        this.setRemainingAttributesFromSeconds();
    }

    private void setRemainingAttributesFromSeconds () {
        this.setYearsFromSeconds();
        this.setMonthsFromSeconds();
        this.setDaysFromSeconds();
        this.setHoursFromSeconds();
        this.setMinutesFromSeconds();
    }

    private void setYearsFromSeconds () {
        this.years = this.seconds / SECONDS_IN_YEAR;
        this.seconds = this.seconds % SECONDS_IN_YEAR;
    }

    private void setMonthsFromSeconds () {
        this.months = this.seconds / SECONDS_IN_MONTH;
        this.seconds = this.seconds % SECONDS_IN_MONTH;
    }

    private void setDaysFromSeconds () {
        this.days = this.seconds / DateTimeConstants.SECONDS_PER_DAY;
        this.seconds = this.seconds % DateTimeConstants.SECONDS_PER_DAY;
    }

    private void setHoursFromSeconds () {
        this.hours = this.seconds / DateTimeConstants.SECONDS_PER_HOUR;
        this.seconds = this.seconds % DateTimeConstants.SECONDS_PER_HOUR;
    }

    private void setMinutesFromSeconds () {
        this.minutes = this.seconds / DateTimeConstants.SECONDS_PER_MINUTE;
        this.seconds = this.seconds % DateTimeConstants.SECONDS_PER_MINUTE;
    }

    @Override
    public String toString () {
        StringBuilder builder = new StringBuilder();
        PrintCommand printCommand = new PrinterCommandBuilder().build();
        printCommand.printWith(builder);
        return builder.toString();
    }

    private enum TimeUnit {
        YEAR(MessageSeeds.PRETTY_PRINT_TIMEDURATION_YEAR_SINGULAR, MessageSeeds.PRETTY_PRINT_TIMEDURATION_YEAR_PLURAL),
        MONTH(MessageSeeds.PRETTY_PRINT_TIMEDURATION_MONTH_SINGULAR, MessageSeeds.PRETTY_PRINT_TIMEDURATION_MONTH_PLURAL),
        DAY(MessageSeeds.PRETTY_PRINT_TIMEDURATION_DAY_SINGULAR, MessageSeeds.PRETTY_PRINT_TIMEDURATION_DAY_PLURAL),
        HOUR(MessageSeeds.PRETTY_PRINT_TIMEDURATION_HOUR_SINGULAR, MessageSeeds.PRETTY_PRINT_TIMEDURATION_HOUR_PLURAL),
        MINUTE(MessageSeeds.PRETTY_PRINT_TIMEDURATION_MINUTE_SINGULAR, MessageSeeds.PRETTY_PRINT_TIMEDURATION_MINUTE_PLURAL),
        SECOND(MessageSeeds.PRETTY_PRINT_TIMEDURATION_SECOND_SINGULAR, MessageSeeds.PRETTY_PRINT_TIMEDURATION_SECOND_PLURAL);

        private MessageSeed singular;
        private MessageSeed plural;

        TimeUnit(MessageSeed singular, MessageSeed plural) {
            this.singular = singular;
            this.plural = plural;
        }

        public void appendValue (int value, StringBuilder builder, Thesaurus thesaurus) {
            if (this.isPlural(value)) {
                this.append(builder, value, thesaurus, this.plural.getKey());
            }
            else {
                this.append(builder, value, thesaurus, this.singular.getKey());
            }
        }

        private boolean isPlural (int value) {
            return value == 0 || value > 1;
        }

        private void append (StringBuilder builder, int value, Thesaurus thesaurus, String translationKey) {
            String pattern = thesaurus.getString(translationKey, translationKey);
            builder.append(MessageFormat.format(pattern, value));
        }

    }
    private class PrinterCommandBuilder {

        private PrintCommand build () {
            List<PrintCommand> commands = new ArrayList<>();
            this.buildCommands(commands);
            this.linkCommands(commands);
            return commands.get(0);
        }

        private void buildCommands (List<PrintCommand> commands) {
            this.addCommandIfNotZero(commands, TimeUnit.YEAR, years);
            this.addCommandIfNotZero(commands, TimeUnit.MONTH, months);
            this.addCommandIfNotZero(commands, TimeUnit.DAY, days);
            this.addCommandIfNotZero(commands, TimeUnit.HOUR, hours);
            this.addCommandIfNotZero(commands, TimeUnit.MINUTE, minutes);
            if (commands.isEmpty()) {
                this.addCommand(commands, TimeUnit.SECOND, seconds);
            }
            else {
                this.addCommandIfNotZero(commands, TimeUnit.SECOND, seconds);
            }
        }

        private void addCommandIfNotZero(List<PrintCommand> commands, TimeUnit timeUnit, int value) {
            if (value > 0) {
                this.addCommand(commands, timeUnit, value);
            }
        }

        private void addCommand(List<PrintCommand> commands, TimeUnit timeUnit, int value) {
            commands.add(new PrintCommand(timeUnit, value));
        }

        private void linkCommands (List<PrintCommand> commands) {
            Collections.reverse(commands);
            PrintCommand nextCommand = new FinalPrintCommand();
            for (PrintCommand command : commands) {
                command.setNext(nextCommand);
                nextCommand = command;
            }
            Collections.reverse(commands);
        }

    }

    private class PrintCommand {
        private int value;
        private TimeUnit timeUnit;
        private PrintCommand next;

        private PrintCommand () {
            super();
        }

        private PrintCommand(TimeUnit timeUnit, int value) {
            super();
            this.value = value;
            this.timeUnit = timeUnit;
        }

        private void setNext (PrintCommand next) {
            this.next = next;
        }

        protected void printWith (StringBuilder builder) {
            this.timeUnit.appendValue(this.value, builder, thesaurus);
            this.next.addSeparator(builder);
            this.next.printWith(builder);
        }

        protected void addSeparator (StringBuilder builder) {
            this.next.addSeparatorIfNotLast(builder);
        }

        protected void addSeparatorIfNotLast (StringBuilder builder) {
            builder.append(thesaurus.getString(MessageSeeds.PRETTY_PRINT_TIMEDURATION_SEPARATOR.getKey(), ", "));
        }
    }

    private final class FinalPrintCommand extends PrintCommand {
        @Override
        protected void printWith (StringBuilder builder) {
            // The final command never prints since it represents the null value
        }

        @Override
        protected void addSeparator (StringBuilder builder) {
            // There is not other command after the final command so no separator to add
        }

        @Override
        protected void addSeparatorIfNotLast (StringBuilder builder) {
            builder.append(thesaurus.getString(MessageSeeds.PRETTY_PRINT_TIMEDURATION_LAST_SEPARATOR.getKey(), " and "));
        }
    }

}