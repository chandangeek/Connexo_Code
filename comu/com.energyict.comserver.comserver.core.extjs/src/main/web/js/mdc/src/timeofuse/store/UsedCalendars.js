Ext.define('Mdc.timeofuse.store.UsedCalendars', {
    extend: 'Ext.data.Store',
    requires: [
        'Uni.model.timeofuse.Calendar'
    ],
    model: 'Uni.model.timeofuse.Calendar',
    storeId: 'usedCalendarStore',
    proxy: {
        type: 'rest',
        url: "",//TODO,
        reader: {
            type: 'json',
            root: 'calendars'
        }
    }
});