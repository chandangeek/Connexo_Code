/**
 * @class Uni.view.search.Results
 */
Ext.define('Uni.view.search.Results', {
    extend: 'Ext.grid.Panel',
    xtype: 'uni-view-search-results',

    requires: [
        'Uni.grid.column.Action',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Uni.store.search.Results',
        'Uni.view.search.ColumnPicker',
        'Uni.grid.plugin.ShowConditionalToolTip'
    ],
    plugins: [{
        ptype: 'showConditionalToolTip',
        pluginId: 'showConditionalToolTipId'
    }
    ],
    store: 'Uni.store.search.Results',
    forceFit: true,
    enableColumnMove: true,
    columns: [],
    config: {
        service: null
    },

    initComponent: function () {
        var me = this,
            service = me.getService(),
            searchFields = Ext.getStore('Uni.store.search.Fields');

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('search.results.paging.displayMsg', 'UNI', '{0} - {1} of {2} search results'),
                displayMoreMsg: Uni.I18n.translate('search.results.paging.displayMoreMsg', 'UNI', '{0} - {1} of more than {2} search results'),
                emptyMsg: Uni.I18n.translate('search.results.paging.emptyMsg', 'UNI', 'There are no search results to display'),
                items: [
                    {
                        xtype: 'button',
                        itemId: 'btn-search-results-count',
                        text: Uni.I18n.translate('general.count', 'UNI', 'Count'),
                        action: 'count'
                    },
                    {
                        xtype: 'uni-search-column-picker',
                        itemId: 'column-picker',
                        grid: me
                    }
                ]
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('search.overview.paging.itemsPerPageMsg', 'UNI', 'Search results per page'),
                dock: 'bottom',
                deferLoading: true,
                pageSizeStore: Ext.create('Ext.data.Store', {
                    fields: ['value'],
                    data: [
                        {value: '10'},
                        {value: '20'},
                        {value: '50'},
                        {value: '100'},
                        {value: '1000'}
                    ]
                })
            }
        ];

        var storeListeners = searchFields.on('load', function (store, items) {
            me.down('uni-search-column-picker').setColumns(items.map(function (field) {
                return service.createColumnDefinitionFromModel(field)
            }));
        }, me, {
            destroyable: true
        });

        var serviceListeners = [];
        serviceListeners.push(service.on('applyFilters', function() {
            me.down('pagingtoolbartop').resetPaging();
            me.down('pagingtoolbarbottom').resetPaging();
            me.down('button[action=count]').setText(Uni.I18n.translate('general.count', 'UNI', 'Count'));
            me.down('button[action=count]').setDisabled(false);
        }, me, {
            destroyable: true
        }));

        serviceListeners.push(service.on('count', function(count){
            me.down('button[action=count]').setText(count.numberOfSearchResults);
            me.down('button[action=count]').setDisabled(true);
            me.setLoading(false);
        }, me, {
            destroyable: true
        }));

        serviceListeners.push(service.on('loadingcount', function(){
            me.setLoading(true);
        }, me, {
            destroyable: true
        }));

        me.callParent(arguments);
        me.on('destroy', function(){
            storeListeners.destroy();
            Ext.Array.each(serviceListeners,function(serviceListener){
                serviceListener.destroy();
            });
        });


    }
});

