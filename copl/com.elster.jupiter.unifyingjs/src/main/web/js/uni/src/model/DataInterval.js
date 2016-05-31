Ext.define('Uni.model.DataInterval', {
    extend: 'Ext.data.Model',
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

        return moment(intervalEnd).subtract(all.timeUnit, all.count).toDate();
    }
});