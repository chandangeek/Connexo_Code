Ext.define('Mdc.store.SecuritySettingsOfDevice', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.DeviceSecuritySetting'
    ],
    model: 'Mdc.model.DeviceSecuritySetting',
    storeId: 'SecuritySettingsOfDevice',
    proxy: {
        type: 'rest',
        url: '/api/ddr/devices/{deviceId}/securityproperties',
        reader: {
            type: 'json',
            root: 'securityPropertySets'
        }
    }
});
