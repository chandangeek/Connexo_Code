Ext.define('Mdc.timeofuse.store.UnusedCalendars', {
    extend: 'Ext.data.Store',
    requires: [
        'Uni.model.timeofuse.Calendar'
    ],
    model: 'Uni.model.timeofuse.Calendar',
    storeId: 'unusedCalendarStore',
    proxy: {
        type: 'rest',
        url: "",//TODO,
        reader: {
            type: 'json',
            root: 'calendars'
        }
    }
});