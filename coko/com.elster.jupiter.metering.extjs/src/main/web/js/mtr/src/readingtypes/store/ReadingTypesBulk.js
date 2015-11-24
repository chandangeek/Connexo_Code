Ext.define('Mtr.readingtypes.store.ReadingTypesBulk', {
    extend: 'Uni.data.store.Filterable',
    requires: ['Mtr.readingtypes.model.ReadingType'],
    model: 'Mtr.readingtypes.model.ReadingType',
    storeId: 'ReadingTypesBulk',
    autoLoad: false,
    buffered: true,
    pageSize: 200,
    remoteFilter: true
});