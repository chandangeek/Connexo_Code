/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * Created by david on 6/6/2016.
 */
Ext.define('Uni.util.ScheduleToStringConverter', {
    singleton: true,

    daysOfWeekStore: Ext.create('Uni.store.DaysOfWeek'),

    convert: function (temporalExpression) {
        if (temporalExpression !== null && temporalExpression !== '') {
            var count = temporalExpression.every.count,
                formattedSchedule;
            switch (temporalExpression.every.timeUnit) {
                case 'seconds':
                    formattedSchedule = Uni.I18n.translatePlural('general.every.seconds', count, 'UNI', 'Every {0} seconds', 'Every second', 'Every {0} seconds');
                    break;
                case 'minutes':
                    formattedSchedule = Uni.I18n.translatePlural('general.every.minutes', count, 'UNI', 'Every {0} minutes', 'Every minute', 'Every {0} minutes');
                    break;
                case 'hours':
                    formattedSchedule = Uni.I18n.translatePlural('general.every.hours', count, 'UNI', 'Every {0} hours', 'Every hour', 'Every {0} hours');
                    break;
                case 'days':
                    formattedSchedule = Uni.I18n.translatePlural('general.every.days', count, 'UNI', 'Every {0} days', 'Every day', 'Every {0} days');
                    break;
                case 'weeks':
                    formattedSchedule = Uni.I18n.translatePlural('general.every.weeks', count, 'UNI', 'Every {0} weeks', 'Every week', 'Every {0} weeks');
                    break;
                case 'months':
                    formattedSchedule = Uni.I18n.translatePlural('general.every.months', count, 'UNI', 'Every {0} months', 'Every month', 'Every {0} months');
                    break;
                case 'years':
                    formattedSchedule = Uni.I18n.translatePlural('general.every.years', count, 'UNI', 'Every {0} years', 'Every year', 'Every {0} years');
                    break;
            }
            return formattedSchedule + this.formatOffset(temporalExpression);
        } else {
            return undefined;
        }
    },

    formatOffset: function (temporalExpression) {
        var offset = temporalExpression.offset,
            result = '',
            seconds,
            minutes,
            hours,
            days,
            dayOfWeek;

        if (!!offset) {
            switch (temporalExpression.every.timeUnit) {
                case 'minutes':
                    seconds = offset.count;

                    if (seconds) {
                        result += ' ' + Uni.I18n.translate('scheduleToStringConverter.at', 'UNI', 'at') + ' ';
                        result += Ext.String.format(Uni.I18n.translatePlural('general.timeUnit.seconds', seconds, 'UNI', '{0} seconds', '{0} second', '{0} seconds'), seconds);
                    }
                    break;
                case 'hours':
                    seconds = (offset.count % 3600) % 60;
                    minutes = Math.floor((offset.count % 3600) / 60);

                    if (minutes || seconds) {
                        result += ' ' + Uni.I18n.translate('scheduleToStringConverter.at', 'UNI', 'at') + ' ';
                        if (minutes) {
                            result += Ext.String.format(Uni.I18n.translatePlural('general.timeUnit.minutes', minutes, 'UNI', '{0} minutes', '{0} minute', '{0} minutes'), minutes);
                        }
                        if (minutes && seconds) {
                            result += ' ' + Uni.I18n.translate('scheduleToStringConverter.and', 'UNI', 'and') + ' ';
                        }
                        if (seconds) {
                            result += Ext.String.format(Uni.I18n.translatePlural('general.timeUnit.seconds', seconds, 'UNI', '{0} seconds', '{0} second', '{0} seconds'), seconds);
                        }
                    }
                    break;
                case 'days':
                    seconds = (offset.count % 3600) % 60;
                    minutes = Math.floor((offset.count % 3600) / 60);
                    hours = Math.floor(offset.count / 3600);

                    result += this.formatTime(hours, minutes, seconds);

                    break;
                case 'weeks':
                    seconds = ((offset.count % 86400) % 3600) % 60;
                    minutes = Math.floor(((offset.count % 86400) % 3600) / 60);
                    hours = Math.floor((offset.count % 86400) / 3600);
                    dayOfWeek = this.daysOfWeekStore.getById((Math.floor(offset.count / 86400)) + 1).get('translation');

                    if (dayOfWeek) {
                        result += ' ' + Uni.I18n.translate('scheduleToStringConverter.on', 'UNI', 'on') + ' ' + dayOfWeek;
                    }

                    result += this.formatTime(hours, minutes, seconds);

                    break;
                case 'months':
                    seconds = ((offset.count % 86400) % 3600) % 60;
                    minutes = Math.floor(((offset.count % 86400) % 3600) / 60);
                    hours = Math.floor((offset.count % 86400) / 3600);
                    days = Math.floor((offset.count + 86400) / 86400);

                    if (temporalExpression.lastDay === false) {
                        result += days ? ' ' + Ext.String.format(Uni.I18n.translate('scheduleToStringConverter.onTheDay', 'UNI', 'on day {0}'), days) : '';
                    } else {
                        result += ' ' + Uni.I18n.translate('scheduleToStringConverter.onTheLastDay', 'UNI', 'on the last day');
                    }

                    result += this.formatTime(hours, minutes, seconds);

                    break;
            }
        }
        return result;
    },

    formatTime: function (hours, minutes, seconds) {
        var result = '';

        if (hours || minutes || seconds) {
            result += ' ' + Uni.I18n.translate('scheduleToStringConverter.at', 'UNI', 'at') + ' ';
            if (hours) {
                result += Uni.I18n.translatePlural('general.timeUnit.hours', hours, 'UNI', '{0} hours', '{0} hour', '{0} hours');
            }
            if (hours && (minutes || seconds)) {
                result += ', '
            }
            if (minutes) {
                result += Uni.I18n.translatePlural('general.timeUnit.minutes', minutes, 'UNI', '{0} minutes', '{0} minute', '{0} minutes');
            }
            if (minutes && seconds) {
                result += ' ' + Uni.I18n.translate('scheduleToStringConverter.and', 'UNI', 'and') + ' ';
            }
            if (seconds) {
                result += Uni.I18n.translatePlural('general.timeUnit.seconds', seconds, 'UNI', '{0} seconds', '{0} second', '{0} seconds');
            }
        }

        return result;
    }
});

