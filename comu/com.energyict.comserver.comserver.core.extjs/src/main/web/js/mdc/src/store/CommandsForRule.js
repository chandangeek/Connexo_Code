Ext.define('Mdc.store.CommandsForRule', {
    extend: 'Ext.data.Store',
    autoLoad: false,
    model: 'Mdc.model.Command',
    storeId: 'CommandsForRule',
    requires: [
        'Mdc.model.Command'
    ]
});