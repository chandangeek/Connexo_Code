package com.energyict.mdc.engine.impl.monitor;

import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.UserEnvironment;
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

    private int seconds;
    private int minutes;
    private int hours;
    private int days;
    private int months;
    private int years;

    public PrettyPrintTimeDuration (TimeDuration timeDuration) {
        super();
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

    private class PrinterCommandBuilder {

        private PrintCommand build () {
            List<PrintCommand> commands = new ArrayList<>();
            this.buildCommands(commands);
            this.linkCommands(commands);
            return commands.get(0);
        }

        private void buildCommands (List<PrintCommand> commands) {
            this.addCommandIfNotZero(commands, years, "PrettyPrintTimeDuration.year.singular", "PrettyPrintTimeDuration.year.plural");
            this.addCommandIfNotZero(commands, months, "PrettyPrintTimeDuration.month.singular", "PrettyPrintTimeDuration.month.plural");
            this.addCommandIfNotZero(commands, days, "PrettyPrintTimeDuration.day.singular", "PrettyPrintTimeDuration.day.plural");
            this.addCommandIfNotZero(commands, hours, "PrettyPrintTimeDuration.hour.singular", "PrettyPrintTimeDuration.hour.plural");
            this.addCommandIfNotZero(commands, minutes, "PrettyPrintTimeDuration.minute.singular", "PrettyPrintTimeDuration.minute.plural");
            if (commands.isEmpty()) {
                this.addCommand(commands, seconds, "PrettyPrintTimeDuration.second.singular", "PrettyPrintTimeDuration.second.plural");
            }
            else {
                this.addCommandIfNotZero(commands, seconds, "PrettyPrintTimeDuration.second.singular", "PrettyPrintTimeDuration.second.plural");
            }
        }

        private void addCommandIfNotZero (List<PrintCommand> commands, int value, String singularTranslationKey, String pluralTranslationKey) {
            if (value > 0) {
                this.addCommand(commands, value, singularTranslationKey, pluralTranslationKey);
            }
        }

        private void addCommand (List<PrintCommand> commands, int value, String singularTranslationKey, String pluralTranslationKey) {
            commands.add(new PrintCommand(value, singularTranslationKey, pluralTranslationKey));
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
        private String singularTranslationKey;
        private String pluralTranslationKey;
        private PrintCommand next;

        private PrintCommand () {
            super();
        }

        private PrintCommand (int value, String singularTranslationKey, String pluralTranslationKey) {
            super();
            this.value = value;
            this.singularTranslationKey = singularTranslationKey;
            this.pluralTranslationKey = pluralTranslationKey;
        }

        private void setNext (PrintCommand next) {
            this.next = next;
        }

        protected void printWith (StringBuilder builder) {
            if (this.isPlural(this.value)) {
                this.print(builder, this.value, this.pluralTranslationKey);
            }
            else {
                this.print(builder, this.value, this.singularTranslationKey);
            }
            this.next.addSeparator(builder);
            this.next.printWith(builder);
        }

        private void print (StringBuilder builder, int value, String translationKey) {
            String pattern = UserEnvironment.getDefault().getTranslation(translationKey);
            builder.append(MessageFormat.format(pattern, value));
        }

        private boolean isPlural (int value) {
            return value == 0 || value > 1;
        }

        protected void addSeparator (StringBuilder builder) {
            this.next.addSeparatorIfNotLast(builder);
        }

        protected void addSeparatorIfNotLast (StringBuilder builder) {
            builder.append(UserEnvironment.getDefault().getTranslation("PrettyPrintTimeDuration.separator"));
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
            builder.append(UserEnvironment.getDefault().getTranslation("PrettyPrintTimeDuration.lastSeparator"));
        }
    }

}