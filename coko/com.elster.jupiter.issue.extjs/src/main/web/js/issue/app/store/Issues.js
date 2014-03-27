Ext.define('Isu.store.Issues', {
    extend: 'Ext.data.Store',
    requires: [
        'Ext.data.proxy.Rest',
        'Uni.component.filter.store.Filterable',
        'Uni.component.sort.store.Sortable'
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
        "beforeLoad": function() {
            var extraParams = this.proxy.extraParams;

            // replace filter extra params with new ones
            if (this.proxyFilter) {
                extraParams = _.omit(extraParams, this.proxyFilter.getFields());
                Ext.merge(extraParams, this.getFilterParams());
            }

            if (this.proxySort) {
                extraParams = _.omit(extraParams, this.proxySort.getFields());
                Ext.merge(extraParams, this.getSortParams());
            }

            if (this.group) {
                Ext.merge(extraParams, this.getGroupParams());
            }

            this.proxy.extraParams = extraParams;
        }
    }
});