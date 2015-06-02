Ext.define('Fim.model.LogFilter', {
    extend: 'Ext.data.Model',
    requires: ['Uni.data.proxy.QueryStringProxy'],
    proxy: {
        type: 'querystring',
        root: 'filter'
    },
    fields: [
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