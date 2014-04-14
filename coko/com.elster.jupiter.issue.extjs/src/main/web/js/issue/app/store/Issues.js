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
        "beforeLoad": function() {
            if (!this.proxyFilter || !this.proxySort) {
                this.loadDefaults();
            }
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
    },

    loadDefaults: function () {
        var defaultFilter = new Isu.model.IssueFilter(),
            store = defaultFilter.status(),
            me = this;

        // this is status "Open"
        var model = new Isu.model.IssueStatus({
            id: 1, name: "Open"
        });
        store.add(model); //todo: hardcoded value! remove after proper REST API is implemented.
        me.proxyFilter = defaultFilter;

        var defaultSort = new Isu.model.IssueSort();
        defaultSort.addSortParam('dueDate');
        me.proxySort = defaultSort;

        defaultFilter.isDefault = true;  //todo: not good option

        this.fireEvent('updateProxyFilter', defaultFilter);
        this.fireEvent('updateProxySort', defaultSort);
    }
});