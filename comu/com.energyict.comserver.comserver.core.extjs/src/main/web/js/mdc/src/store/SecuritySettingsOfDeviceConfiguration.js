Ext.define('Mdc.store.SecuritySettingsOfDeviceConfiguration', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.SecuritySetting'
    ],
    model: 'Mdc.model.SecuritySetting',
    storeId: 'SecuritySettingsOfDeviceConfiguration',
    proxy: {
        type: 'rest',
        timeout: 300000,
        url: '/api/dtc/devicetypes/{deviceType}/deviceconfigurations/{deviceConfig}/securityproperties',
        pageParam: false,
        startParam: false,
        limitParam: false,
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});