Ext.define('Mdc.store.DevicesSelectedBulk', {
    extend: 'Uni.data.store.Filterable',
    autoLoad : false,

    requires: [
        'Mdc.model.Device'
    ],

    model: 'Mdc.model.Device',
    storeId: 'DevicesSelectedBulk'
});
