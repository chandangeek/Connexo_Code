Ext.define('Mdc.store.EncryptionLevels', {
    extend: 'Ext.data.Store',
    storeId: 'encryptionLevels',
    requires: [
        'Mdc.model.EncryptionLevel'
    ],
    model: 'Mdc.model.EncryptionLevel',
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/dtc/devicetypes/{deviceType}/deviceconfigurations/{deviceConfig}/securityproperties/enclevels',
        reader: {
            type: 'json',
            root: 'data'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    },
    listeners: {
        load: {
            fn: function (store, records, success) {
                if (success && records && records.length == 0){
                    store.add(Mdc.model.EncryptionLevel.noEncryption());
                }
            }
        }
    }
});