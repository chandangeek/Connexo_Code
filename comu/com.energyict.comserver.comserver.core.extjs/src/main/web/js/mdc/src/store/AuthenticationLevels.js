Ext.define('Mdc.store.AuthenticationLevels', {
    extend: 'Ext.data.Store',
    storeId: 'authenticationLevels',
    requires: [
        'Mdc.model.AuthenticationLevel'
    ],
    model: 'Mdc.model.AuthenticationLevel',
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/dtc/devicetypes/{deviceType}/deviceconfigurations/{deviceConfig}/securityproperties/authlevels',
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
                    store.add(Mdc.model.AuthenticationLevel.noAuthentication());
                }
            }
        }
    }
});