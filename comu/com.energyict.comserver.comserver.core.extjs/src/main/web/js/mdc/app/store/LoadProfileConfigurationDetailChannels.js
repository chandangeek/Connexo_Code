Ext.define('Mdc.store.LoadProfileConfigurationDetailChannels', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.Channel'
    ],
    model: 'Mdc.model.Channel',
    storeId: 'LoadProfileConfigurationDetailChannels',
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/dtc/devicetypes/{deviceType}/deviceconfigurations/{deviceConfig}/loadprofileconfigurations/{loadProfileConfiguration}/channels',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});
