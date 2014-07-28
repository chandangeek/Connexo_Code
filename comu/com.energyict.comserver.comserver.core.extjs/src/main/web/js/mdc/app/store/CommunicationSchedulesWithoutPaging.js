Ext.define('Mdc.store.CommunicationSchedulesWithoutPaging',{
    extend: 'Mdc.store.CommunicationSchedules',
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