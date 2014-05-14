Ext.define('Mdc.store.LogbookConfigurations', {
    extend: 'Ext.data.Store',
    requires: [
        'Ext.data.proxy.Rest',
        'Mdc.model.LogbookConfigurations'
    ],
    model: 'Mdc.model.LogbookConfigurations',
    storeId: 'LogbookConfigurations',
    autoLoad: false
});
