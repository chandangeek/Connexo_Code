Ext.define('Dal.store.CreationRuleActions', {
    extend: 'Ext.data.Store',
    requires: [
        'Ext.data.proxy.Rest'
    ],
    model: 'Dal.model.Action',
    autoLoad: false,
    proxy: {
        type: 'rest',
        url: '/api/dal/actions',
        reader: {
            type: 'json',
            root: 'ruleActionTypes'
        },
        pageParam: false,
        startParam: false,
        limitParam: false
    }
});
