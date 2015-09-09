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
                Uni.I18n.translate('general.time.hours_short', 'UNI', '{0}h', [hours]) :
                Uni.I18n.translatePlural('general.time.hours', hours, 'UNI', '{0} hours', '{0} hour', '{0} hours');
            format += ' ';
        }

        if (duration.asMinutes() > 1) {
            minutes = duration.minutes();
            format += shortFormat ?
                Uni.I18n.translate('general.time.minutes_short', 'UNI', '{0}m', [minutes]) :
                Uni.I18n.translatePlural('general.time.minutes', minutes, 'UNI', '{0} minutes', '{0} minute', '{0} minutes');
            format += ' ';
        }

        seconds = duration.seconds() + Math.round(duration.milliseconds() / 1000);

        // If less than 1 full second, round up; milliseconds get ignored in this case.
        if (duration.asSeconds() < 1 && duration.asSeconds() !== 0) {
            seconds = 1;
        }

        format += shortFormat ?
            Uni.I18n.translate('general.time.seconds_short', 'UNI', '{0}s', [seconds]) :
            Uni.I18n.translatePlural('general.time.seconds', seconds, 'UNI', '{0} seconds', '{0} second', '{0} seconds');

        if (duration.asHours() === 1 && duration.asMinutes() === 60 && duration.asSeconds() === 3600) {
            format = shortFormat ?
                Uni.I18n.translate('general.time.hours_short', 'UNI', '{0}h', [1]) :
                Uni.I18n.translatePlural('general.time.hours', 1, 'UNI', '{0} hours', '{0} hour', '{0} hours');
        } else if (duration.asHours() < 1 && duration.asMinutes() === 1 && duration.asSeconds() === 60) {
            format = shortFormat ?
                Uni.I18n.translate('general.time.minutes_short', 'UNI', '{0}m', [1]) :
                Uni.I18n.translatePlural('general.time.minutes', 1, 'UNI', '{0} minutes', '{0} minute', '{0} minutes');
        }

        return format;
    },

    /**
     * Uses a regular expression to find and replace all instances of a parameter.
     *
     * @param {String} param Parameter to find and replace the index parameters in
     * @param {Number} searchIndex Index value to replace with the value
     * @param {String} replaceValue Value to replace search results with
     * @returns {String} Replaced parameter
     */
    replaceAll: function (param, searchIndex, replaceValue) {
        var lookup = '\{[' + searchIndex + ']\}';
        return param.replace(new RegExp(lookup, 'g'), replaceValue);
    }
});