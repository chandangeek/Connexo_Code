Ext.define('Mdc.store.MasterDeviceCandidates', {
    extend: 'Ext.data.Store',
    storeId: 'MasterDeviceCandidates',
    autoLoad: false,
    fields: ['id', 'name'],
    proxy: {
        type: 'ajax',
        url: '/api/ddr/field/gateways',
        pageParam: false,
        startParam: false,
        limitParam: false,
        reader: {
            type: 'json',
            root: 'gateways'
        }
    }
});