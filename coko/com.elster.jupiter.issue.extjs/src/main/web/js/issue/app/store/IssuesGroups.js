Ext.define('Isu.store.IssuesGroups', {
    extend: 'Ext.data.Store',
    model: 'Isu.model.IssuesGroups',
    pageSize: 10,
    autoLoad: false,

    listeners: {
        beforeload: function () {
            this.getProxy().setExtraParam('issueType', 'datacollection');
        }
    }
 });