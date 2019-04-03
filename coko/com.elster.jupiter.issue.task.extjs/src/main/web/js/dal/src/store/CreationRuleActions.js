Ext.define('Itk.store.CreationRuleActions', {
    extend: 'Ext.data.Store',
    requires: [
        'Ext.data.proxy.Rest'
    ],
    model: 'Itk.model.Action',
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/itk/actions',
        reader: {
            type: 'json',
            root: 'ruleActionTypes'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});
