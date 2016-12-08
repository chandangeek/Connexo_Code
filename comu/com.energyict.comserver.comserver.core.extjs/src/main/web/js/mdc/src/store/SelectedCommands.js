Ext.define('Mdc.store.SelectedCommands',{
    extend: 'Ext.data.Store',
    requires: [
        'Mdc.model.Command'
    ],
    model: 'Mdc.model.Command',
    autoLoad: false
});
