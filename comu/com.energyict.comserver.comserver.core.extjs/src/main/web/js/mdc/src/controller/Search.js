/**
 * @class MdcApp.controller.Main
 */
Ext.define('Mdc.controller.Search', {
    extend: 'Uni.controller.Search',

    init: function () {
        var me = this,
            router = this.getController('Uni.controller.history.Router'),
            searchResults = Ext.getStore('Uni.store.search.Results');

        me.callParent(arguments);
        me.on('beforegridconfigure', function(fields, columns){
            var cid = _.pluck(columns, 'dataIndex').indexOf('mRID');
            var column = columns[cid];
            if (column) {
                column.renderer = function (value) {
                    var url = router.getRoute('devices/device').buildUrl({mRID: value});
                    return '<a href="{0}">{1}</a>'.replace('{0}', url).replace('{1}', Ext.String.htmlEncode(value));
                }
            }
        });

        searchResults.on('load', function(store, items){
            var grid = me.getResultsGrid();
            var btn = grid.down('#search-bulk-actions-button');
            btn.setDisabled(!(me.searchDomain && me.searchDomain.getId() === "com.energyict.mdc.device.data.Device" && items && items.length));
        });
    },

    showOverview: function () {
        var me = this;

        me.callParent(arguments);
        var grid = me.getResultsGrid();
        grid.down('pagingtoolbartop').insert(3, {
            xtype: 'button',
            text: 'Bulk actions',
            itemId: 'search-bulk-actions-button',
            handler: me.showBulkAction,
            scope: me
        });
    },

    showBulkAction: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router');

        router.getRoute('search/bulkAction').forward();

        //me.getSearchItems().up().setLoading(true);
        //me.saveState();

        //var searchCriteria = me.getFilters();
        //var store = me.getStore('Mdc.store.Devices');
        //Ext.Object.merge(searchCriteria,store.getProxy().extraParams);

        //me.getStore('Mdc.store.DevicesSelectedBulk').setProxy(store.getProxy());
        //me.getStore('Mdc.store.DevicesSelectedBulk').getProxy().extraParams.limit = 1000;
        //me.getStore('Mdc.store.DevicesSelectedBulk').load({
        //    callback: function(){

        //    }
        //});
    }
});
