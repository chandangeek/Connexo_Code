Ext.define('Mdc.store.AvailableCommunicationSchedulesForDevice',{
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.CommunicationSchedule'
    ],
    model: 'Mdc.model.CommunicationSchedule',
    storeId: 'AvailableCommunicationSchedulesForDevice',
    remoteSort: true,
    proxy: {
        type: 'rest',
        url: '../../api/scr/schedules',
        reader: {
            type: 'json',
            root: 'schedules'
        },

        pageParam: false,
        startParam: false,
        limitParam: false
    }
});
