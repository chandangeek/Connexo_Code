Ext.define('Mdc.store.CodeTables', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.CodeTable'
    ],
    autoLoad: true,
    model: 'Mdc.model.CodeTable',
    storeId: 'CodeTables',

    proxy: {
        type: 'rest',
        url: '../../api/mdc/codetables',
        reader: {
            type: 'json',
            root: 'Code'
        }
    }
});