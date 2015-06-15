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
        'Uni.store.search.Results'
    ],

    store: 'Uni.store.search.Results',
    columns: [],

    initComponent: function () {
        var me = this;

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('search.results.paging.displayMsg', 'UNI', '{0} - {1} of {2} search results'),
                displayMoreMsg: Uni.I18n.translate('search.results.paging.displayMoreMsg', 'UNI', '{0} - {1} of more than {2} search results'),
                emptyMsg: Uni.I18n.translate('search.results.paging.emptyMsg', 'UNI', 'There are no search results to display')
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                itemsPerPageMsg: Uni.I18n.translate('search.overview.paging.itemsPerPageMsg', 'UNI', 'Search results per page'),
                dock: 'bottom',
                deferLoading: true
            }
        ];

        me.callParent(arguments);
    }
});

