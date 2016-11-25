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
        url: '/api/ddr/devices/{deviceId}/schedules',
        reader: {
            type: 'json',
            root: 'schedules'
        }
    }
});
