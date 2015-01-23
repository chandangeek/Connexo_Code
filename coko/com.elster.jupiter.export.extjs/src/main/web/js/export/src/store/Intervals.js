Ext.define('Dxp.store.Intervals', {
    extend: 'Ext.data.Store',
    model: 'Dxp.model.Interval',
    storeId: 'Intervals',
    requires: [
        'Dxp.model.Interval'
    ],
    autoLoad: false,

    proxy: {
        type: 'rest',
        url: '/api/mtr/fields/intervals',
        reader: {
            type: 'json',
            root: 'intervals'
        },

        pageParam: false,
        startParam: false,
        limitParam: false
    }
});
