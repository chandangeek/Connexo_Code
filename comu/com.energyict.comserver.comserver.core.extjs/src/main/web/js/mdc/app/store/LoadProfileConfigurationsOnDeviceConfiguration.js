Ext.define('Mdc.store.LoadProfileConfigurationsOnDeviceConfiguration', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.LoadProfileConfiguration'
    ],
    autoLoad: false,
    model: 'Mdc.model.LoadProfileConfiguration',

    proxy: {
        type: 'rest',
        url: '/api/dtc/devicetypes/{deviceType}/deviceconfigurations/{deviceConfig}/loadprofileconfigurations',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});