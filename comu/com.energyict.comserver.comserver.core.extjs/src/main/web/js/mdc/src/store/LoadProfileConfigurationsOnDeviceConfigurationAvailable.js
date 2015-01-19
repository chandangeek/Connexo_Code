Ext.define('Mdc.store.LoadProfileConfigurationsOnDeviceConfigurationAvailable', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.LoadProfileType'
    ],
    autoLoad: false,
    model: 'Mdc.model.LoadProfileType',
    proxy: {
        type: 'rest',
        url: '/api/dtc/devicetypes/{deviceType}/deviceconfigurations/{deviceConfig}/loadprofileconfigurations/available',
        pageParam: false,
        startParam: false,
        limitParam: false,
        reader: {
            type: 'json',
            root: 'data'
        }
    }

});