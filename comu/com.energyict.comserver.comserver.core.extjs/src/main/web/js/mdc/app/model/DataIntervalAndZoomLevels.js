Ext.define('Mdc.model.DataIntervalAndZoomLevels', {
    extend: 'Ext.data.Model',
    requires: [
        'Mdc.model.LoadProfileDataDuration'
    ],
    fields: [
        'interval',
        'all',
        'intervalInMs',
        'zoomLevels',
        'duration'
    ],
    idProperty: 'interval',

    getIntervalStart: function (intervalEnd) {
        var all = this.get('all'),
            intervalEndDate = new Date(intervalEnd),
            intervalEndDateTime = intervalEndDate.getTime(),
            intervalStart;

        switch (all.timeUnit) {
            case 'minutes':
                intervalStart = intervalEndDateTime - all.count * 60000;
                break;
            case 'hours':
                intervalStart = intervalEndDateTime - all.count * 1440000;
                break;
            case 'days':
                intervalStart = intervalEndDateTime - all.count * 86400000;
                break;
            case 'weeks':
                intervalStart = intervalEndDateTime - all.count * 604800000;
                break;
            case 'months':
                intervalStart = intervalEndDate.setMonth(intervalEndDate.getMonth() - all.count);
                break;
            case 'years':
                intervalStart = intervalEndDate.setFullYear(intervalEndDate.getFullYear() - all.count);
                break;
        }

        return intervalStart;
    }
});