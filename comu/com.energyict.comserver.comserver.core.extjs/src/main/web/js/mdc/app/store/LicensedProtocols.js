Ext.define('Mdc.store.LicensedProtocols', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.LicensedProtocol'
    ],
    autoLoad: true,
    model: 'Mdc.model.LicensedProtocol',
    storeId: 'LicensedProtocols',

    proxy: {
        type: 'rest',
        url: '../../api/mdc/licensedprotocols',
        reader: {
            type: 'json',
            root: 'LicensedProtocol'
        }
    }
});