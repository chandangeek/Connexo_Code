Ext.define('Mdc.store.EncryptionLevels', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.EncryptionLevel'
    ],

    model: 'Mdc.model.EncryptionLevel',
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '../../api/dtc/devicetypes/{deviceType}/deviceconfigurations/{deviceConfig}/securityproperties/enclevels',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});