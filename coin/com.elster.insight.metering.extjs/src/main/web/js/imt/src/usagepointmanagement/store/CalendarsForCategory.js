Ext.define('Imt.usagepointmanagement.store.CalendarsForCategory', {
    extend: 'Ext.data.Store',
    fields: ['id', 'name'],
    remoteFilter: true,
    //filters: [
    //    {
    //        property: 'status',
    //        value: 'ACTIVE'
    //    },
    //    {
    //        property: 'category',
    //        value: 'TOU'
    //    }
    //],
    proxy: {
        type: 'rest',
        url: '/api/cal/calendars',
        reader: {
            type: 'json',
            root: 'calendars'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});