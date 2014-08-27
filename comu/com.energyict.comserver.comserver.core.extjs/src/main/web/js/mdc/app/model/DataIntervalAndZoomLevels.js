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
        var all = this.get('all');

        return moment(intervalEnd).subtract(all.timeUnit, all.count).valueOf();
    }
});