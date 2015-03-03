Ext.define('Mdc.store.DeviceSchedules', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.DeviceSchedule'
    ],
    model: 'Mdc.model.DeviceSchedule',
    storeId: 'DeviceSchedules',
    proxy: {
        type: 'rest',
        limitParam: false,
        pageParam: false,
        startParam: false,
        urlTpl: '/api/ddr/devices/{mRID}/schedules',
        reader: {
            type: 'json',
            root: 'schedules'
        },
        setUrl: function (mRID) {
            this.url = this.urlTpl.replace('{mRID}', encodeURIComponent(mRID));
        }
    }

});
