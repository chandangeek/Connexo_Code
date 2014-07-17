Ext.define('Mdc.util.ScheduleToStringConverter',{
    singleton: true,
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
        var offset = temporalExpression.offset;
        switch(temporalExpression.every.timeUnit){
            case 'minutes':
                return Ext.util.Format.format(
                    Uni.I18n.translate('scheduleToStringConverter.minuteOffser', 'MDC', ' at {0} seconds')
                    ,offset.count);
            case 'hours':
                return Ext.util.Format.format(
                    Uni.I18n.translate('scheduleToStringConverter.hourOffset', 'MDC', ' at {0} minutes and {1} seconds')
                    ,Math.floor(offset.count/60)
                    ,(offset.count%60));
            case 'days':
                return Ext.util.Format.format(
                    Uni.I18n.translate('scheduleToStringConverter.dayOffset', 'MDC', ' at {0} hours , {1} minutes and {2} seconds')
                    ,Math.floor(offset.count/3600)
                    ,Math.floor((offset.count%3600)/60)
                    ,(offset.count%3600)%60);
            case 'weeks':
                return '';
            case 'months':
                if(temporalExpression.lastDay===false){
                    return Ext.util.Format.format(
                        Uni.I18n.translate('scheduleToStringConverter.monthOffset', 'MDC', ' on the {0} day at {1} hours, {2} minutes and {3} seconds')
                        ,Math.floor(Math.floor(offset.count/86400))
                        ,Math.floor((offset.count%86400)/3600)
                        ,((offset.count%86400)%3600)/60
                        ,((offset.count%86400)%3600)%60);
                } else {
                    return Ext.util.Format.format(
                    Uni.I18n.translate('scheduleToStringConverter.monthOffset', 'MDC', ' on the last day at {0} hours, {1} minutes and {2} seconds')
                        ,Math.floor((offSet.count%86400)/3600)
                        ,((offset.count%86400)%3600)/60
                        ,((offset.count%86400)%3600)%60);
                }
        }
        return '';
    }
});
