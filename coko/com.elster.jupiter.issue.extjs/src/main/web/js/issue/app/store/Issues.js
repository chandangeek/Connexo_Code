Ext.define('Isu.store.Issues', {
    extend: 'Ext.data.Store',
    requires: [
        'Ext.data.proxy.Rest',
        'Uni.component.filter.store.Filterable',
        'Uni.component.sort.store.Sortable',
        'Isu.model.IssueFilter'
    ],

    mixins: [
        'Uni.component.filter.store.Filterable',
        'Uni.component.sort.store.Sortable'
    ],

    model: 'Isu.model.Issues',
    pageSize: 10,
    autoLoad: false,

    group: null,

    setGroup: function(record) {
        this.group = record;
    },

    getGroupParams: function() {
        return {
            'reason': this.group.getId()
        }
    },

    listeners: {
        beforeload: function () {
            this.getProxy().setExtraParam('issueType', 'datacollection');
        }
    }
});