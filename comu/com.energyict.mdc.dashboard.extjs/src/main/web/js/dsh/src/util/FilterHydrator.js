Ext.define('Dsh.util.FilterHydrator', {
    extract: function (record) {
        var data = {},
            startedBetween = record.startedBetween,
            finishedBetween = record.finishedBetween;
        Ext.merge(data, record.getData());
        if (!_.isEmpty(startedBetween)) {
            data.startedBetween = {};

            if (startedBetween.get('from')) {
                data.startedBetween.from = {
                    date: startedBetween.get('from'),
                    hours: startedBetween.get('from').getHours(),
                    minutes: startedBetween.get('from').getMinutes()
                };
            }

            if (startedBetween.get('to')) {
                data.startedBetween.to = {
                    date: startedBetween.get('to'),
                    hours: startedBetween.get('to').getHours(),
                    minutes: startedBetween.get('to').getMinutes()
                };
            }
        }

        if (!_.isEmpty(finishedBetween)) {
            data.finishedBetween = {};
            if (finishedBetween.get('from')) {
                data.finishedBetween.from = {
                    date: finishedBetween.get('from'),
                    hours: finishedBetween.get('from').getHours(),
                    minutes: finishedBetween.get('from').getMinutes()
                }
            }
            if (finishedBetween.get('to')) {
                data.finishedBetween.to = {
                    date: finishedBetween.get('to'),
                    hours: finishedBetween.get('to').getHours(),
                    minutes: finishedBetween.get('to').getMinutes()
                }
            }
        }

        return data;
    },

    hydrate: function (data, record) {
        var startedBetween = data.startedBetween,
            finishedBetween = data.finishedBetween,
            startedBetweenFromDate = this.parseDate(startedBetween.from.date, startedBetween.from.hours, startedBetween.from.minutes),
            startedBetweenToDate = this.parseDate(startedBetween.to.date, startedBetween.to.hours, startedBetween.to.minutes),
            finishedBetweenFromDate = this.parseDate(finishedBetween.from.date, finishedBetween.from.hours, finishedBetween.from.minutes),
            finishedBetweenToDate = this.parseDate(finishedBetween.to.date, finishedBetween.to.hours, finishedBetween.to.minutes);
        delete data.startedBetween;
        delete data.finishedBetween;
        record.set(data);
        record.startedBetween = Ext.create('Dsh.model.DateRange', { from: startedBetweenFromDate, to: startedBetweenToDate });
        record.finishedBetween = Ext.create('Dsh.model.DateRange', { from: finishedBetweenFromDate, to: finishedBetweenToDate });
    },

    parseDate: function (date, hours, minutes) {
        if (!date) return null;
        var parsedDate = Ext.Date.parseDate(date, Uni.util.Preferences.lookup(Uni.DateTime.dateShortKey, Uni.DateTime.dateShortDefault)),
            parsedDateInMills = moment(parsedDate).valueOf(),
            hoursInMills = hours * 3600000,
            minutesInMills = minutes * 60000,
            resultDateInMills = parsedDateInMills + hoursInMills + minutesInMills;
        return moment(resultDateInMills).toDate();
    }
});