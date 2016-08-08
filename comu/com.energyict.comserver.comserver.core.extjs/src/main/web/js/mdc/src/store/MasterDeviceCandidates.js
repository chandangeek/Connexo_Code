Ext.define('Mdc.store.MasterDeviceCandidates', {
    extend: 'Ext.data.Store',
    storeId: 'MasterDeviceCandidates',
    autoLoad: false,
    fields: ['id', 'name'],
    pageSize: 50,
    proxy: {
        type: 'ajax',
        url: '/api/ddr/field/gateways',
        reader: {
            type: 'json',
            root: 'gateways'
        }
    }
});