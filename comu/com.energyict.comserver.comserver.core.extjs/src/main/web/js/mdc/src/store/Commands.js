Ext.define('Mdc.store.Commands',{
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.Command'
    ],
    model: 'Mdc.model.Command',
    storeId: 'Commands',
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/crr/commandrules/commands',
        reader: {
            type: 'json'
        },
        pageParam: undefined,
        startParam: undefined,
        limitParam: undefined
    }
});