Ext.define('Mdc.store.LicensedProtocols', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.LicensedProtocol'
    ],
    model: 'Mdc.model.LicensedProtocol',
    storeId: 'LicensedProtocols',

    proxy: {
        type: 'rest',
        url: '../../api/plr/licensedprotocols',
        reader: {
            type: 'json',
            root: 'LicensedProtocol'
        }
    }
});