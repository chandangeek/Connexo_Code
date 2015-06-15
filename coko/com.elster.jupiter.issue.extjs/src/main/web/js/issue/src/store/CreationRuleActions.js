Ext.define('Isu.store.CreationRuleActions', {
    extend: 'Ext.data.Store',
    requires: [
        'Ext.data.proxy.Rest'
    ],
    model: 'Isu.model.Action',
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/isu/actions',
        reader: {
            type: 'json',
            root: 'ruleActionTypes'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});
