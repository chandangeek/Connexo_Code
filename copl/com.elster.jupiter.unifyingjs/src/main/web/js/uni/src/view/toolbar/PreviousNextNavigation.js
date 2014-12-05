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
     */
    itemsName: Uni.I18n.translate('general.items', 'UNI', 'Items').toLowerCase(),

    /**
     * @cfg {String} [itemsName="items"]
     * Route to items list.
     */
    listRoute: null,

    initComponent: function () {
        var me = this,
            store = Ext.getStore(me.store);

        me.callParent(arguments);

        if (me.router && me.routerIdArgument && store && store.getCount() > 1) {
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
                iconCls: 'icon-arrow-up',
                iconAlign: 'left',
                style: 'margin-right: 0 !important;'
            },
            nextBtn = {
                itemId: 'previous-next-navigation-toolbar-next-link',
                ui: 'plain',
                iconCls: 'icon-arrow-down',
                iconAlign: 'right'
            },
            separator = {
                xtype: 'tbtext',
                text: '|'
            },
            currentIndex = store.indexOfId(me.router.arguments[me.routerIdArgument]),
            itemsCounter = {
                xtype: 'component',
                cls: ' previous-next-navigation-items-counter'
            },
            storeCurrentPage = store.lastOptions.page;

        if (currentIndex === -1) {
            currentIndex = store.indexOfId(parseInt(me.router.arguments[me.routerIdArgument]));
        }

        if (me.listRoute && me.router.getRoute(me.listRoute)) {
            me.router.queryParams.limit = store.pageSize;
            me.router.queryParams.start = store.pageSize * storeCurrentPage;
            me.itemsName = '<a href="' + me.router.getRoute(me.listRoute).buildUrl(me.router.arguments, me.router.queryParams) + '">' + me.itemsName + '</a>';
        }

        if (store.getTotalCount() > store.pageSize * storeCurrentPage) {
            itemsCounter.html = Ext.String.format(Uni.I18n.translate('previousNextNavigation.displayMsgMoreItems', 'UNI', '{0} of more than {1}'), store.pageSize * (storeCurrentPage - 1) + currentIndex + 1, store.pageSize * storeCurrentPage) + ' ' + me.itemsName;
        } else {
            itemsCounter.html = Ext.String.format(Uni.I18n.translate('previousNextNavigation.displayMsgItems', 'UNI', '{0} of {1}'), store.pageSize * (storeCurrentPage - 1) + currentIndex + 1, store.getTotalCount()) + ' ' + me.itemsName;
        }

        if (currentIndex - 1 >= 0) {
            me.router.arguments[me.routerIdArgument] = store.getAt(currentIndex - 1).getId();
            prevBtn.href = me.router.getRoute(me.router.currentRoute).buildUrl(me.router.arguments, me.router.queryParams);
        } else if (storeCurrentPage > 1) {
            prevBtn.handler = function () {
                me.setLoading(true);
                store.loadPage(storeCurrentPage - 1, {
                    scope: me,
                    callback: function (records) {
                        me.setLoading(false);
                        if (records.length) {
                            me.router.arguments[me.routerIdArgument] = store.getAt(records.length - 1).getId();
                            me.router.getRoute(me.router.currentRoute).forward(me.router.arguments, me.router.queryParams);
                        } else {
                            prevBtn.disable();
                        }
                    }
                });
            }
        } else {
            prevBtn.disabled = true;
        }

        if (currentIndex + 1 < store.getCount()) {
            me.router.arguments[me.routerIdArgument] = store.getAt(currentIndex + 1).getId();
            nextBtn.href = me.router.getRoute(me.router.currentRoute).buildUrl(me.router.arguments, me.router.queryParams);
        } else if (store.getTotalCount() > store.pageSize * storeCurrentPage) {
            nextBtn.handler = function () {
                me.setLoading(true);
                store.loadPage(storeCurrentPage + 1, {
                    scope: me,
                    callback: function (records) {
                        me.setLoading(false);
                        if (records.length) {
                            me.router.arguments[me.routerIdArgument] = store.getAt(0).getId();
                            me.router.getRoute(me.router.currentRoute).forward(me.router.arguments, me.router.queryParams);
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