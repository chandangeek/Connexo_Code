Ext.define('Mdc.store.SecuritySettingsOfDeviceConfiguration', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.SecuritySetting'
    ],

    model: 'Mdc.model.SecuritySetting',

    proxy: {
        type: 'rest',
        url: '/api/dtc/devicetypes/{deviceType}/deviceconfigurations/{deviceConfig}/securityproperties',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});