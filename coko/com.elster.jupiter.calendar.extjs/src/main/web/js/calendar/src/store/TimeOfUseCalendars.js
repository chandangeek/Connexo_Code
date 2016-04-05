Ext.define('Cal.store.TimeOfUseCalendars', {
    extend: 'Ext.data.Store',
    model: 'Uni.model.timeofuse.Calendar',
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/cal/calendars/timeofusecalendars',
        timeout: 120000,
        reader: {
            type: 'json'
        }
    }
});
