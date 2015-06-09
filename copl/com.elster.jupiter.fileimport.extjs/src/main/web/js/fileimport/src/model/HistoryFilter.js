Ext.define('Fim.model.HistoryFilter', {
    extend: 'Ext.data.Model',
    requires: ['Uni.data.proxy.QueryStringProxy'],
    proxy: {
        type: 'querystring',
        root: 'filter'
    },
    fields: [
        {name: 'importService', type: 'auto'},
        {name: 'startedOnFrom', type: 'number', useNull: true},
        {name: 'startedOnTo', type: 'number', useNull: true},
        {name: 'finishedOnFrom', type: 'number', useNull: true},
        {name: 'finishedOnTo', type: 'number', useNull: true},
        {name: 'status', type: 'auto'},
        {name: 'sorting', type: 'auto'}
    ],

    proxy: {
        type: 'querystring',
        root: 'filter',
        destroy: function () {
            var filter = new this.router.filter.self({
                sorting: this.router.filter.get('sorting')
            });
            filter.save();
        }
    }

});