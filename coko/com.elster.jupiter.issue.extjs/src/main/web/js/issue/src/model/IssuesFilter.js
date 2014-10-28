Ext.define('Isu.model.IssuesFilter', {
    extend: 'Ext.data.Model',
    requires: [
        'Uni.data.proxy.QueryStringProxy'
    ],
    fields: [
        'status',
        'assignee',
        'reason',
        'meter',
        'grouping',
        'sorting'
    ],

    proxy: {
        type: 'querystring',
        root: 'filter',
        destroy: function () {
            var filter = Ext.decode(this.router.queryParams[this.root]);

            Ext.iterate(filter, function (key, value) {
                if (key !== 'grouping' && key !== 'sorting') {
                    filter[key] = '';
                }
            });

            this.router.queryParams[this.root] = Ext.encode(filter);
            this.router.getRoute().forward(this.router.arguments, this.router.queryParams);
        }
    }
});