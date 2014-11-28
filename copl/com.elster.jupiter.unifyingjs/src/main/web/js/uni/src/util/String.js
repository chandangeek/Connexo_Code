/**
 * @class Uni.util.String
 *
 * Utility class for commonly used string functions.
 */
Ext.define('Uni.util.String', {
    singleton: true,

    /**
     * Formats a duration in milliseconds to a human readable format.
     *
     * @param millis
     * @returns {string}
     */
    formatDuration: function (millis, shortFormat) {
        var duration = moment.duration(millis),
            format = '',
            hours,
            minutes,
            seconds;

        if (duration.asHours() > 1) {
            hours = Math.floor(duration.asHours()); // Avoids rounding errors.
            format += shortFormat ?
                Uni.I18n.translatePlural('general.time.hours_short', hours, 'UNI', '{0}h') :
                Uni.I18n.translatePlural('general.time.hours', hours, 'UNI', '{0} hours');
            format += ' ';
        }

        if (duration.asMinutes() > 1) {
            minutes = duration.minutes();
            format += shortFormat ?
                Uni.I18n.translatePlural('general.time.minutes_short', minutes, 'UNI', '{0}m') :
                Uni.I18n.translatePlural('general.time.minutes', minutes, 'UNI', '{0} minutes');
            format += ' ';
        }

        seconds = duration.seconds() + Math.round(duration.milliseconds() / 1000);

        // If less than 1 full second, round up; milliseconds get ignored in this case.
        if (duration.asSeconds() < 1 && duration.asSeconds() !== 0) {
            seconds = 1;
        }

        format += shortFormat ?
            Uni.I18n.translatePlural('general.time.seconds_short', seconds, 'UNI', '{0}s') :
            Uni.I18n.translatePlural('general.time.seconds', seconds, 'UNI', '{0} seconds');

        return format;
    }
});