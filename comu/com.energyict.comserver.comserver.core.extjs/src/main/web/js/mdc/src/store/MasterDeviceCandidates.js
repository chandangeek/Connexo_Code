Ext.define('Mdc.store.MasterDeviceCandidates', {
    extend: 'Ext.data.Store',
    storeId: 'MasterDeviceCandidates',
    autoLoad: false,
    fields: ['id', 'name'],
    pageSize: 30,
    proxy: {
        type: 'ajax',
        url: '/api/ddr/field/gateways',
        reader: {
            type: 'json',
            root: 'gateways'
        }
    }
});