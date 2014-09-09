Ext.define('Mdc.store.AuthenticationLevels', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.AuthenticationLevel'
    ],

    model: 'Mdc.model.AuthenticationLevel',
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '../../api/dtc/devicetypes/{deviceType}/deviceconfigurations/{deviceConfig}/securityproperties/authlevels',
        reader: {
            type: 'json',
            root: 'data'
        }
    }
});