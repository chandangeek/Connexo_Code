Ext.define('Mdc.timeofuseondevice.store.AllowedCalendars', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.timeofuseondevice.model.AllowedCalendar'
    ],
    model: 'Mdc.timeofuseondevice.model.AllowedCalendar',
    autoLoad: false,

    proxy: {
        type: 'rest',
        urlTpl: "/api/ddr/devices/{mRID}/timeofuse/availablecalendars",
        reader: {
            type: 'json'
        },

        setUrl: function (mRID) {
            this.url = this.urlTpl.replace('{mRID}', mRID);
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});