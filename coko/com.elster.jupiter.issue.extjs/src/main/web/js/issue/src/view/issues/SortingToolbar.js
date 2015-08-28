Ext.define('Isu.view.issues.SortingToolbar', {
    extend: 'Uni.view.panel.FilterToolbar',
    requires: [
        'Uni.view.button.SortItemButton',
        'Isu.view.issues.SortingMenu'
    ],
    alias: 'widget.issues-sorting-toolbar',
    title: Uni.I18n.translate('general.sort', 'ISU', 'Sort'),
    emptyText: Uni.I18n.translate('general.none', 'ISU', 'None'),
    showClearButton: false,
    store: null,

    initComponent: function () {
        var me = this;

        me.dockedItems = [
            {
                xtype: 'header',
                dock: 'left'
            },
            {
                xtype: 'container',
                dock: 'right',
                minHeight: 150,
                items: [
                    {
                        itemId: 'Reset',
                        xtype: 'button',
                        style: {
                            marginRight: '0px !important'
                        },
                        text: Uni.I18n.translate('issues.clearAll','ISU','Clear all'),
                        action: 'clear'
                    },
                    {
                        itemId: 'addSort',
                        xtype: 'button',
                        style: {
                            marginRight: '0px !important'
                        },
                        //      ui: 'sort',
                        text: Uni.I18n.translate('issues.addSort','ISU','Add sort'),
                        action: 'addSort',
                        menu: {
                            xtype: 'issues-sorting-menu',
                            itemId: 'issues-sorting-menu',
                            listeners: {
                                click: {
                                    fn: Ext.bind(me.addSortItem, me)
                                }
                            }
                        }
                    }
                ]
            }
        ];

        me.store = Ext.getStore(me.store) || Ext.create(me.store);

        me.callParent(arguments);

        me.addButtons();
    },

    addButtons: function () {
        var me = this,
            queryString = Uni.util.QueryString.getQueryStringValues(false),
            buttonsContainer = me.getContainer(),
            buttons = [];

        Ext.suspendLayouts();
        buttonsContainer.removeAll();
        Ext.Array.each(queryString.sort, function (sortItem) {
            var menuItem,
                type,
                cls;

            if (sortItem.indexOf('-') === 0) {
                type = sortItem.slice(1);
                cls = 'x-btn-sort-item-desc';
            } else {
                type = sortItem;
                cls = 'x-btn-sort-item-asc';
            }

            menuItem = me.down('#issues-sorting-menu [action=' + type + ']');
            buttons.push({
                xtype: 'sort-item-btn',
                itemId: 'issues-sort-by-' + type + '-button',
                text: menuItem.text,
                sortValue: sortItem,
                iconCls: cls,
                listeners: {
                    closeclick: {
                        fn: Ext.bind(me.removeSortItem, me)
                    },
                    click: {
                        fn: Ext.bind(me.changeSortDirection, me)
                    }
                }
            });
        });
        if (buttons.length) {
            buttonsContainer.add(buttons);
        }
        Ext.resumeLayouts(true);
    },

    addSortItem: function (menu, menuItem) {
        var me = this,
            queryString = Uni.util.QueryString.getQueryStringValues(false);

        queryString.sort = queryString.sort || [];
        queryString.sort = Ext.isArray(queryString.sort) ? queryString.sort : [queryString.sort];

        if (!Ext.Array.contains(queryString.sort, menuItem.action)) {
            queryString.sort.push(menuItem.action);
            me.applyNewState(queryString);
        }
    },

    removeSortItem: function (button) {
        var me = this,
            queryString = Uni.util.QueryString.getQueryStringValues(false);

        if (Ext.isArray(queryString.sort)) {
            Ext.Array.remove(queryString.sort, button.sortValue);
        } else if (queryString.sort === button.sortValue) {
            queryString.sort = undefined;
        }

        me.applyNewState(queryString);
    },

    changeSortDirection: function (button) {
        var me = this,
            queryString = Uni.util.QueryString.getQueryStringValues(false);

        queryString.sort = Ext.isArray(queryString.sort) ? queryString.sort : [queryString.sort];

        for (var i = 0; i < queryString.sort.length; i++) {
            if (queryString.sort[i] === button.sortValue) {
                if (button.sortValue.indexOf('-') === 0) {
                    queryString.sort[i] = button.sortValue.slice(1);
                } else {
                    queryString.sort[i] = '-' + button.sortValue;
                }
                break;
            }
        }

        me.applyNewState(queryString);
    },

    applyNewState: function (queryString) {
        var me = this,
            href = Uni.util.QueryString.buildHrefWithQueryString(queryString, false);

        if (window.location.href !== href) {
            Uni.util.History.setParsePath(false);
            Uni.util.History.suspendEventsForNextCall();
            window.location.href = href;
            Ext.util.History.currentToken = window.location.hash.substr(1);
            me.addButtons();
            me.store.load();
        }
    }
});