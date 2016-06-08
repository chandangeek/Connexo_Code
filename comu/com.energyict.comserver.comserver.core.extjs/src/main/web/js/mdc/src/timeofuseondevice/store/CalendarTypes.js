Ext.define('Mdc.timeofuseondevice.store.CalendarTypes', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.timeofuseondevice.model.CalendarType'
    ],
    model: 'Mdc.timeofuseondevice.model.CalendarType',
    autoLoad: false,

    proxy: {
        type: 'rest',
        url: "/api/ddr/field/calendartypes",
        reader: {
            type: 'json',
            root: 'calendarTypes'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});