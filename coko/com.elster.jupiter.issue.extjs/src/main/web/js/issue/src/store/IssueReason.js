Ext.define('Isu.store.IssueReason', {
    extend: 'Ext.data.Store',
    model: 'Isu.model.IssueReason',
    autoLoad: false,

    listeners: {
        beforeload: function () {
            this.getProxy().setExtraParam('issueType', 'datacollection');
        }
    }
});