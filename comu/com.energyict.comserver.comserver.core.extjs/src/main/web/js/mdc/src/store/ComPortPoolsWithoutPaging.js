Ext.define('Mdc.store.ComPortPoolsWithoutPaging',{
    extend: 'Mdc.store.ComPortPools',
    requires: [
        'Mdc.store.ComPortPools'
    ],

    storeId: 'comPortPoolsWithoutPaging',

    proxy: {
        type: 'rest',
        url: '../../api/mdc/comportpools',
        reader: {
            type: 'json',
            root: 'data'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});
