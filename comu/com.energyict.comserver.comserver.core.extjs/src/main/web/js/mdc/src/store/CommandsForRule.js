Ext.define('Mdc.store.CommandsForRule', {
    extend: 'Ext.data.Store',
    autoLoad: false,
    model: 'Mdc.model.Command',
    sorters: [
        {
            property: 'commandName',
            direction: 'ASC'
        }
    ],
    storeId: 'CommandsForRule',
    requires: [
        'Mdc.model.Command'
    ]
});