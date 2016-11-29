Ext.define('Mdc.store.DeviceConfigurationResults',{
    extend: 'Ext.data.Store',
    model: 'Mdc.model.ValidationResults',
    storeId: 'DeviceTypes',
    proxy: {
        type: 'rest',
        url: '/api/ddr/devices/{deviceId}/validationrulesets/validationmonitoring/configurationview',
        reader: {
            type: 'json'
        }
    }
});
