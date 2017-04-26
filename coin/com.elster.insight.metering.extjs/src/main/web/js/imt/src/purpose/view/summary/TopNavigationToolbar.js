/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */


Ext.define('Imt.purpose.view.summary.TopNavigationToolbar', {
    extend: 'Ext.toolbar.Toolbar',
    alias: 'widget.purpose-top-navigation-toolbar',
    layout: {
        type: 'hbox',
        pack: 'end'
    },

    displayMsg: Uni.I18n.translate('general.displayMsgItems', 'UNI', '{0} - {1} of {2} items'),

    store: null,


    initComponent: function(){
        var me = this,
            store = Ext.getStore(me.store),
            storeTotal = store.getTotalCount(),
            storeCount =  store.getCount();

        me.items = ['->',
            {
                xtype: 'tbtext',
                itemId: 'displayItem',
                text: Uni.I18n.translate('general.displayMsgOutputs', 'UNI', '{0} - {1} of {2} outputs', ['1', storeCount, storeTotal])
            },
            {
                itemId: 'previous-next-navigation-toolbar-previous-link',
                ui: 'plain',
                iconCls: 'uni-icon-arrow-up',
                style: 'margin-right: 0 !important;',
                handler: function(){
                    store.previousPage();
                }
            },
            {
                itemId: 'previous-next-navigation-toolbar-next-link',
                ui: 'plain',
                iconCls: 'uni-icon-arrow-down',
                handler: function(){
                    store.nextPage();
                }
            }];



        // me.down('#displayItem').setText(me.displayMsg);



        me.callParent(arguments);

    }
    // @private
    // initComponent: function (store) {
    //     var me = this,
    //         prevBtn =
    //         nextBtn = {
    //             itemId: 'previous-next-navigation-toolbar-next-link',
    //             ui: 'plain',
    //             iconCls: 'uni-icon-arrow-down'
    //         },
    //         itemsCounter = {
    //             xtype: 'component',
    //             cls: ' previous-next-navigation-items-counter'
    //         },
    //         routeArguments = Ext.clone(me.router.arguments),
    //         queryParams = Ext.clone(me.router.queryParams),
    //         indexContainer = me.indexLocation == 'arguments' ? routeArguments : queryParams,
    //         currentIndex = store.indexOfId(indexContainer[me.routerIdArgument]),
    //         storeCurrentPage = store.lastOptions ? store.lastOptions.page : 1,
    //         storePageSize = store.pageSize,
    //         storeTotal = store.getTotalCount(),
    //         storeCount =  store.getCount();
    //
    //     if (me.clearQueryParams) {
    //         queryParams = {}
    //     }
    //
    //     if (currentIndex === -1) {
    //         currentIndex = store.indexOfId(parseInt(indexContainer[me.routerIdArgument]));
    //     }
    //
    //     if (!queryParams[me.totalProperty] && me.isFullTotalCount) {
    //         queryParams[me.totalProperty] = storeTotal;
    //     } else if (!queryParams[me.totalProperty] || Math.abs(queryParams[me.totalProperty]) < storeTotal) {
    //         queryParams[me.totalProperty] = storePageSize * storeCurrentPage >= storeTotal ? storeTotal : -(storeTotal - 1);
    //     }
    //
    //     if(storeCount <= 1){
    //         itemsCounter.html = Ext.String.format(Uni.I18n.translate('previousNextNavigation.displayMsgItems', 'UNI', '{0} of {1}'), 1, 1 + ' ' + me.itemsName);
    //     }
    //     else if (queryParams[me.totalProperty] < 0) {
    //         itemsCounter.html = Ext.String.format(Uni.I18n.translate('previousNextNavigation.displayMsgMoreItems', 'UNI', '{0} of more than {1}'), storePageSize * (storeCurrentPage - 1) + currentIndex + 1, -queryParams[me.totalProperty]) + ' ' + me.itemsName;
    //     } else {
    //         itemsCounter.html = Ext.String.format(Uni.I18n.translate('previousNextNavigation.displayMsgItems', 'UNI', '{0} of {1}'), storePageSize * (storeCurrentPage - 1) + currentIndex + 1, queryParams[me.totalProperty]) + ' ' + me.itemsName;
    //     }
    //
    //     if (currentIndex - 1 >= 0) {
    //         indexContainer[me.routerIdArgument] = store.getAt(currentIndex - 1).getId();
    //         prevBtn.href = me.router.getRoute(me.router.currentRoute).buildUrl(routeArguments, queryParams);
    //     } else if (storeCurrentPage > 1) {
    //         prevBtn.handler = function () {
    //             me.up('#contentPanel').setLoading(true);
    //             store.loadPage(storeCurrentPage - 1, {
    //                 scope: me,
    //                 callback: function (records) {
    //                     me.up('#contentPanel').setLoading(false);
    //                     if (records.length) {
    //                         indexContainer[me.routerIdArgument] = store.getAt(records.length - 1).getId();
    //                         me.router.getRoute(me.router.currentRoute).forward(routeArguments, queryParams);
    //                     } else {
    //                         prevBtn.disable();
    //                     }
    //                 }
    //             });
    //         }
    //     } else {
    //         prevBtn.disabled = true;
    //     }
    //     if (currentIndex + 1 < storeCount) {
    //         indexContainer[me.routerIdArgument] = store.getAt(currentIndex + 1).getId();
    //         nextBtn.href = me.router.getRoute(me.router.currentRoute).buildUrl(routeArguments, queryParams);
    //     } else if (currentIndex + 1 === storeTotal) {
    //         nextBtn.disabled = true;
    //     } else if (storeTotal > storePageSize * storeCurrentPage) {
    //         nextBtn.handler = function () {
    //             me.up('#contentPanel').setLoading(true);
    //             store.loadPage(storeCurrentPage + 1, {
    //                 scope: me,
    //                 callback: function (records) {
    //                     me.up('#contentPanel').setLoading(false);
    //                     if (records.length) {
    //                         indexContainer[me.routerIdArgument] = store.getAt(0).getId();
    //                         me.router.getRoute(me.router.currentRoute).forward(routeArguments, queryParams);
    //                     } else {
    //                         nextBtn.disable();
    //                     }
    //                 }
    //             });
    //         }
    //     } else {
    //         nextBtn.disabled = true;
    //     }
    //
    //     me.add(itemsCounter, prevBtn, nextBtn);
    // }


});