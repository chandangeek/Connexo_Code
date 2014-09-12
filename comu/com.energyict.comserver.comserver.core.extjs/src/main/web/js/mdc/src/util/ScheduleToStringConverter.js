Ext.define('Mdc.util.ScheduleToStringConverter', {
    singleton: true,

    daysOfWeekStore: Ext.create('Mdc.store.DaysOfWeek'),

    convert: function(temporalExpression){
        if(temporalExpression!==null && temporalExpression !== ''){
            var timeUnit = temporalExpression.every.timeUnit,
                count = temporalExpression.every.count,
                formattedSchedule = Ext.String.format('Every {0} {1}', count,  Uni.I18n.translatePlural('general.timeUnit.' + timeUnit, count, 'MDC', timeUnit));
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

        switch(temporalExpression.every.timeUnit){
            case 'minutes':
                seconds = offset.count;

                if (seconds) {
                    result += ' ' + Uni.I18n.translate('scheduleToStringConverter.at', 'MDC', 'at') + ' ';
                    result += Ext.String.format(Uni.I18n.translatePlural('scheduleToStringConverter.seconds', seconds, 'MDC', '{0} seconds'), seconds);
                }
                break;
            case 'hours':
                seconds = (offset.count%3600)%60;
                minutes = Math.floor((offset.count%3600)/60);

                if (minutes || seconds) {
                    result += ' ' + Uni.I18n.translate('scheduleToStringConverter.at', 'MDC', 'at') + ' ';
                    if (minutes) {
                        result += Ext.String.format(Uni.I18n.translatePlural('scheduleToStringConverter.minutes', minutes, 'MDC', '{0} minutes'), minutes);
                    }
                    if (minutes && seconds) {
                        result += ' ' + Uni.I18n.translate('scheduleToStringConverter.and', 'MDC', 'and') + ' ';
                    }
                    if (seconds) {
                        result += Ext.String.format(Uni.I18n.translatePlural('scheduleToStringConverter.seconds', seconds, 'MDC', '{0} seconds'), seconds);
                    }
                }
                break;
            case 'days':
                seconds = (offset.count%3600)%60;
                minutes = Math.floor((offset.count%3600)/60);
                hours = Math.floor(offset.count/3600);

                result += this.formatTime(hours, minutes, seconds);

                break;
            case 'weeks':
                seconds = ((offset.count%86400)%3600)%60;
                minutes = Math.floor(((offset.count%86400)%3600)/60);
                hours = Math.floor((offset.count%86400)/3600);
                dayOfWeek = this.daysOfWeekStore.getById((Math.floor(offset.count/86400))+1).get('translation');

                if (dayOfWeek) {
                    result += ' ' + Uni.I18n.translate('scheduleToStringConverter.on', 'MDC', 'on') + ' ' + dayOfWeek;
                }

                result += this.formatTime(hours, minutes, seconds);

                break;
            case 'months':
                seconds = ((offset.count%86400)%3600)%60;
                minutes = Math.floor(((offset.count%86400)%3600)/60);
                hours = Math.floor((offset.count%86400)/3600);
                days = Math.floor(Math.floor(offset.count/86400));

                if (temporalExpression.lastDay === false) {
                    result += days ? ' ' + Ext.String.format(Uni.I18n.translate('scheduleToStringConverter.onTheDay', 'MDC', 'on the {0} day'), days) : '';
                } else {
                    result += ' ' + Uni.I18n.translate('scheduleToStringConverter.onTheLastDay', 'MDC', 'on the last day');
                }

                result += this.formatTime(hours, minutes, seconds);

                break;
        }
        return result;
    },

    formatTime: function (hours, minutes, seconds) {
        var result = '';

        if (hours || minutes || seconds) {
            result += ' ' + Uni.I18n.translate('scheduleToStringConverter.at', 'MDC', 'at') + ' ';
            if (hours) {
                result += Ext.String.format(Uni.I18n.translatePlural('scheduleToStringConverter.hours', hours, 'MDC', '{0} hours'), hours);
            }
            if (hours && (minutes || seconds)) {
                result += ', '
            }
            if (minutes) {
                result += Ext.String.format(Uni.I18n.translatePlural('scheduleToStringConverter.minutes', minutes, 'MDC', '{0} minutes'), minutes);
            }
            if (minutes && seconds) {
                result += ' ' + Uni.I18n.translate('scheduleToStringConverter.and', 'MDC', 'and') + ' ';
            }
            if (seconds) {
                result += Ext.String.format(Uni.I18n.translatePlural('scheduleToStringConverter.seconds', seconds, 'MDC', '{0} seconds'), seconds);
            }
        }

        return result;
    }
});
