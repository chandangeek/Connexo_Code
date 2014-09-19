Ext.define('Dsh.store.filter.CommunicationSchedule', {
    extend: 'Ext.data.Store',
    fields: ['id', 'name'],
    autoLoad: true,
    proxy: {
        type: 'rest',
        url: '/api/dsr/field/comschedules',
        reader: {
            type: 'json',
            root: 'comSchedules'
        }
    }
});

