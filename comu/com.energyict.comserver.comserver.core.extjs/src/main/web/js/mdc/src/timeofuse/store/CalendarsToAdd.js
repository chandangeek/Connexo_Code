Ext.define('Mdc.timeofuse.store.CalendarsToAdd', {
    extend: 'Ext.data.Store',
    requires: [
        'Uni.model.timeofuse.Calendar'
    ],
    model: 'Uni.model.timeofuse.Calendar',
    storeId: 'calendarsToAddStore',
    proxy: {
        type: 'rest',
        urlTpl: "../../api/dtc/devicetypes/{deviceTypeId}/unusedcalendars",
        reader: {
            type: 'json'
        },

        setUrl: function (deviceTypeId) {
            this.url = this.urlTpl.replace('{deviceTypeId}', deviceTypeId);
        }
    }
});