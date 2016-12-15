Ext.define('Mdc.store.CommandsForRule', {
    extend: 'Ext.data.Store',
    autoLoad: false,
    model: 'Mdc.model.Command',
    sorters: [
        {
            property: 'displayName',
            direction: 'ASC'
        }
    ],
    storeId: 'CommandsForRule',
    requires: [
        'Mdc.model.Command'
    ]
});