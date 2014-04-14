Ext.define('Mdc.store.CodeTables', {
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.CodeTable'
    ],
    model: 'Mdc.model.CodeTable',
    storeId: 'CodeTables',
    autoLoad: true,
    proxy: {
        type: 'rest',
        url: '../../api/plr/codetables',
        reader: {
            type: 'json',
            root: 'Code'
        }
    }
});