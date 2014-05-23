Ext.define('Mdc.store.CommunicationSchedules',{
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.CommunicationSchedule'
    ],
    model: 'Mdc.model.CommunicationSchedule',
    storeId: 'CommunicationSchedules',
    pageSize: 10,
//    sorters: [{
//        property: 'name',
//        direction: 'ASC'
//    }],
    remoteSort: true,
    proxy: {
        type: 'rest',
        url: '../../api/scr/schedules',
        reader: {
            type: 'json',
            root: 'schedules'
        }
    }
});
