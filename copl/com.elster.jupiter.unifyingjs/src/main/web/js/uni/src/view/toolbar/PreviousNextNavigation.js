/**
 * @class Uni.view.toolbar.PreviousNextNavigation
 *
 * This component contains links to previous and next details pages if these exist.
 * So it isn't necessary to return to the list each time.
 *
 * Example usage:
 *
 *     ...
 *     requires: [
 *         ...
 *         'Uni.view.toolbar.PreviousNextNavigation'
 *     ],
 *
 *     initComponent: function () {
 *         var me = this;
 *
 *         me.items = [
 *             {
 *                 xtype: 'previous-next-navigation-toolbar',
 *                 store: 'Mdc.store.RegisterConfigsOfDevice',
 *                 router: me.router,
 *                 routerIdArgument: 'registerId'
 *             },
 *             ...
 *         ]
 *     }
 *     ...
 */
Ext.define('Uni.view.toolbar.PreviousNextNavigation', {
    extend: 'Ext.toolbar.Toolbar',
    alias: 'widget.previous-next-navigation-toolbar',
    layout: {
        type: 'hbox',
        pack: 'end'
    },
    /**
     * @cfg {String} store
     * This parameter is mandatory
     */
    store: null,
    /**
     * @cfg {Uni.controller.history.Router} router
     * This parameter is mandatory
     */
    router: null,
    /**
     * @cfg {String} routerIdArgument
     * This parameter is mandatory
     */
    routerIdArgument: null,

    /**
     * @cfg {String} [itemsName="items"]
     * Name of items.
     * A link to a items list can be provided. For example:
     *
     *     itemsName: '<a href="' + me.router.getRoute('devices/device/registers').buildUrl() + '">' + Uni.I18n.translate('deviceregisterconfiguration.registers', 'MDC', 'Registers').toLowerCase() + '</a>'
     */
    itemsName: Uni.I18n.translate('general.items', 'UNI', 'Items').toLowerCase(),

    /**
     * @cfg {String} [totalProperty="total"]
     * Name of url query parameter for storing total records amount.
     */
    totalProperty: 'total',
    indexLocation: 'arguments',
    isFullTotalCount: false,

    initComponent: function () {
        var me = this,
            store = Ext.getStore(me.store);

        me.callParent(arguments);
        if (me.router && me.routerIdArgument && store) {
            me.initToolbar(store);
        } else {
            me.hide();

            //<debug>
            if (!store) {
                console.error('Store for \'' + me.xtype + '\' is not defined');
            }
            if (!me.router) {
                console.error('Router for \'' + me.xtype + '\' is not defined');
            }
            if (!me.routerIdArgument) {
                console.error('Router id argument for \'' + me.xtype + '\' is not defined');
            }
            //</debug>
        }
    },

    // @private
    initToolbar: function (store) {
        var me = this,
            prevBtn = {
                itemId: 'previous-next-navigation-toolbar-previous-link',
                ui: 'plain',
                iconCls: 'uni-icon-arrow-up',
                style: 'margin-right: 0 !important;'
            },
            nextBtn = {
                itemId: 'previous-next-navigation-toolbar-next-link',
                ui: 'plain',
                iconCls: 'uni-icon-arrow-down'
            },
            itemsCounter = {
                xtype: 'component',
                cls: ' previous-next-navigation-items-counter'
            },
            routeArguments = Ext.clone(me.router.arguments),
            queryParams = Ext.clone(me.router.queryParams),
            indexContainer = me.indexLocation == 'arguments' ? routeArguments : queryParams,
            currentIndex = store.indexOfId(indexContainer[me.routerIdArgument]),
            storeCurrentPage = store.lastOptions?store.lastOptions.page:1,
            storePageSize = store.pageSize,
            storeTotal = store.getTotalCount(),
            storeCount =  store.getCount();

        if (currentIndex === -1) {
            currentIndex = store.indexOfId(parseInt(indexContainer[me.routerIdArgument]));
        }

        if (!queryParams[me.totalProperty] && me.isFullTotalCount) {
            queryParams[me.totalProperty] = storeTotal;
        } else if (!queryParams[me.totalProperty] || Math.abs(queryParams[me.totalProperty]) < storeTotal) {
            queryParams[me.totalProperty] = storePageSize * storeCurrentPage >= storeTotal ? storeTotal : -(storeTotal - 1);
        }

        if(storeCount <= 1){
            itemsCounter.html = Ext.String.format(Uni.I18n.translate('previousNextNavigation.displayMsgItems', 'UNI', '{0} of {1}'), 1, 1 + ' ' + me.itemsName);
        }
        else if (queryParams[me.totalProperty] < 0) {
            itemsCounter.html = Ext.String.format(Uni.I18n.translate('previousNextNavigation.displayMsgMoreItems', 'UNI', '{0} of more than {1}'), storePageSize * (storeCurrentPage - 1) + currentIndex + 1, -queryParams[me.totalProperty]) + ' ' + me.itemsName;
        } else {
            itemsCounter.html = Ext.String.format(Uni.I18n.translate('previousNextNavigation.displayMsgItems', 'UNI', '{0} of {1}'), storePageSize * (storeCurrentPage - 1) + currentIndex + 1, queryParams[me.totalProperty]) + ' ' + me.itemsName;
        }

        if (currentIndex - 1 >= 0) {
            indexContainer[me.routerIdArgument] = store.getAt(currentIndex - 1).getId();
            prevBtn.href = me.router.getRoute(me.router.currentRoute).buildUrl(routeArguments, queryParams);
        } else if (storeCurrentPage > 1) {
            prevBtn.handler = function () {
                me.up('#contentPanel').setLoading(true);
                store.loadPage(storeCurrentPage - 1, {
                    scope: me,
                    callback: function (records) {
                        me.up('#contentPanel').setLoading(false);
                        if (records.length) {
                            indexContainer[me.routerIdArgument] = store.getAt(records.length - 1).getId();
                            me.router.getRoute(me.router.currentRoute).forward(routeArguments, queryParams);
                        } else {
                            prevBtn.disable();
                        }
                    }
                });
            }
        } else {
            prevBtn.disabled = true;
        }
        if (currentIndex + 1 < storeCount) {
            indexContainer[me.routerIdArgument] = store.getAt(currentIndex + 1).getId();
            nextBtn.href = me.router.getRoute(me.router.currentRoute).buildUrl(routeArguments, queryParams);
        } else if (currentIndex + 1 === storeTotal) {
            nextBtn.disabled = true;
        } else if (storeTotal > storePageSize * storeCurrentPage) {
            nextBtn.handler = function () {
                me.up('#contentPanel').setLoading(true);
                store.loadPage(storeCurrentPage + 1, {
                    scope: me,
                    callback: function (records) {
                        me.up('#contentPanel').setLoading(false);
                        if (records.length) {
                            indexContainer[me.routerIdArgument] = store.getAt(0).getId();
                            me.router.getRoute(me.router.currentRoute).forward(routeArguments, queryParams);
                        } else {
                            nextBtn.disable();
                        }
                    }
                });
            }
        } else {
            nextBtn.disabled = true;
        }

        me.add(itemsCounter, prevBtn, nextBtn);
    }
});