Ext.define('Isu.store.Actions', {
    extend: 'Ext.data.Store',
    requires: [
        'Ext.data.proxy.Rest'
    ],

    model: 'Isu.model.Actions',
    autoLoad: false,

    listeners: {
        beforeload: function () {
            this.getProxy().setExtraParam('issueType', 'datacollection');
        }
    }
});
