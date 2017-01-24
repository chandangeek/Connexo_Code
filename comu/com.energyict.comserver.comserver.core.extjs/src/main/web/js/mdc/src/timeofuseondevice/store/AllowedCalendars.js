Ext.define('Mdc.timeofuseondevice.store.AllowedCalendars', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.timeofuseondevice.model.AllowedCalendar'
    ],
    model: 'Mdc.timeofuseondevice.model.AllowedCalendar',
    autoLoad: false,

    proxy: {
        type: 'rest',
        url: "/api/ddr/devices/{deviceId}/timeofuse/availablecalendars",
        reader: {
            type: 'json'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});