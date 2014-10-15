Ext.define('Mdc.store.Domains',{
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.Domain'
    ],
    model: 'Mdc.model.Domain',
    storeId: 'Domains',
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/ddr/field/enddevicedomains',
        reader: {
            type: 'json',
            root: 'domains'
        }
    }
});
