Ext.define('Isu.store.Issues', {
    extend: 'Ext.data.Store',
    requires: [
        'Ext.data.proxy.Rest',
        'Uni.component.filter.store.Filterable',
        'Uni.component.sort.store.Sortable',
        'Isu.model.IssueFilter',
        'Uni.util.Hydrator'
    ],

    mixins: [
        'Uni.component.filter.store.Filterable',
        'Uni.component.sort.store.Sortable'
    ],

    model: 'Isu.model.Issues',
    pageSize: 10,
    autoLoad: false,

    listeners: {
        beforeload: function () {
            this.getProxy().setExtraParam('issueType', 'datacollection');
        }
    }
});