Ext.define('Mdc.store.CommandLimitationRules',{
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.CommandLimitRule'
    ],
    model: 'Mdc.model.CommandLimitRule',
    storeId: 'CommandLimitationRules',
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/crr/commandrules',
        reader: {
            type: 'json',
            root: 'commandrules'
        }
    }
});