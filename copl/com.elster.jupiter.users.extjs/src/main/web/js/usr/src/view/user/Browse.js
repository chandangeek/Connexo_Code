/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Usr.view.user.Browse', {

    // extend: 'Ext.panel.Panel',
    extend: 'Uni.view.container.ContentContainer',

    alias: 'widget.userBrowse',

    itemId: 'userBrowse',

    overflowY: 'auto',

    // ui: 'large',
    //
    // layout: 'card',

    stores: [
        'Uni.store.search.Domains',
        'Uni.store.search.Fields',
        'Uni.store.search.Properties',
        'Uni.store.search.PropertyValues',
        'Uni.store.search.Results'
    ],

    requires: [
        'Usr.view.user.List',
        'Usr.view.user.Details',
        'Usr.view.user.UserActionMenu',
        'Ext.panel.Panel',
        'Uni.view.container.PreviewContainer',
        'Uni.util.FormEmptyMessage',
        'Uni.view.search.Overview',
        'Uni.view.search.field.internal.Adapter',
        'Uni.util.Filters',
        'Uni.view.search.field.Selection',
        'Uni.view.search.field.Simple',
        'Uni.grid.column.search.DeviceType',
        'Uni.grid.column.search.DeviceConfiguration',
        'Uni.grid.column.search.Quantity',
        'Uni.grid.column.search.Boolean',
        'Usr.service.Search',
        'Usr.view.user.UsersGrid'
    ],

    config: {
        service: null
    },

    initComponent: function () {
        var me = this,
            domainsStore = this.getService().getSearchDomainsStore(),
            store = Ext.getStore('Uni.store.search.Properties');

        // me.items = [
        //     {
        //         xtype: 'preview-container',
        //         itenId: 'search-preview-container',
        //         // grid: {
        //         //     xtype: 'userList'
        //         // },
        //         grid: {
        //             xtype: 'users-grid',
        //             itemId: 'users-grid',
        //             service: me.userDomainSearchService
        //         },
        //         emptyComponent: {
        //             xtype: 'uni-form-empty-message',
        //             text: Uni.I18n.translate('user.Browse.empty', 'USR', 'An error occurred while loading the users.')
        //         }
        //         // previewComponent: {
        //         //     xtype: 'userDetails'
        //         // }
        //     }
        // ];

        // me.tbar = {
        //     xtype: 'panel',
        //     items: [
        //         {
        //             itemId: 'step-errors',
        //             xtype: 'uni-form-error-message',
        //             hidden: true
        //         },
        //         {
        //             xtype: 'panel',
        //             itemId: 'grid-filter',
        //             maskElement: 'el',
        //             ui: 'filter',
        //             defaults: {
        //                 xtype: 'panel',
        //                 layout: 'hbox'
        //             },
        //             items: [
        //                 {
        //                     xtype: 'uni-search-internal-criteriapanel',
        //                     itemId: 'search-criteria-sticky',
        //                     layout: 'column',
        //                     lbar: {
        //                         xtype: 'label',
        //                         text: Uni.I18n.translate('searchItems.filter.criteria', 'MDC', 'Criteria'),
        //                         width: 100
        //                     },
        //                     rbar: [{
        //                         disabled: true,
        //                         itemId: 'search-criteria-selector',
        //                         xtype: 'search-criteria-selector',
        //                         service: me.userDomainSearchService
        //                     }],
        //                     margin: '10 0 0 0',
        //                     service: me.userDomainSearchService,
        //                     sticky: true
        //                 },
        //                 {
        //                     xtype: 'uni-search-internal-criteriapanel',
        //                     itemId: 'search-criteria-removable',
        //                     layout: 'column',
        //                     margin: '0 0 0 100',
        //                     service: me.userDomainSearchService
        //                 }
        //             ],
        //             bbar: {
        //                 xtype: 'container',
        //                 layout: {
        //                     type: 'hbox',
        //                     pack: 'end'
        //                 },
        //                 items: [
        //                     {
        //                         xtype: 'button',
        //                         itemId: 'search-button',
        //                         ui: 'action',
        //                         text: Uni.I18n.translate('general.apply', 'MDC', 'Apply'),
        //                         action: 'search'
        //                     },
        //                     {
        //                         xtype: 'button',
        //                         itemId: 'clear-all-button',
        //                         text: Uni.I18n.translate('general.clearAll', 'MDC', 'Clear all'),
        //                         action: 'clearFilters',
        //                         margin: '0 0 0 0',
        //                         disabled: true
        //                     }
        //                 ]
        //             }
        //         }
        //     ]
        // };

        me.content = [
            {
                xtype: 'panel',
                ui: 'large',
                title: Uni.I18n.translate('general.users', 'USR', 'Users'),
                // layout: {
                //     type: 'vbox',
                //     align: 'stretch'
                // },
                items: [
                    {
                        xtype: 'preview-container',
                        // grid: {
                        //     xtype: 'userList'
                        // },
                        grid: {
                            xtype: 'users-grid',
                            itemId: 'users-grid',
                            service: me.getService()
                        },
                        emptyComponent: {
                            xtype: 'uni-form-empty-message',
                            text: Uni.I18n.translate('user.Browse.empty', 'USR', 'An error occurred while loading the users.')
                        },
                        previewComponent: {
                            xtype: 'userDetails'
                        }
                    }
                ],
                tbar: [
                    {
                        xtype: 'panel',
                        items: [
                            {
                                itemId: 'step-errors',
                                xtype: 'uni-form-error-message',
                                hidden: true
                            },
                            {
                                xtype: 'panel',
                                itemId: 'grid-filter',
                                maskElement: 'el',
                                ui: 'filter',
                                defaults: {
                                    xtype: 'panel',
                                    layout: 'hbox'
                                },
                                items: [
                                    {
                                        xtype: 'uni-search-internal-criteriapanel',
                                        itemId: 'search-criteria-sticky',
                                        layout: 'column',
                                        lbar: {
                                            xtype: 'label',
                                            text: Uni.I18n.translate('searchItems.filter.criteria', 'MDC', 'Criteria'),
                                            width: 100
                                        },
                                        rbar: [{
                                            disabled: true,
                                            itemId: 'search-criteria-selector',
                                            xtype: 'search-criteria-selector',
                                            service: me.getService()
                                        }],
                                        margin: '10 0 0 0',
                                        service: me.getService(),
                                        sticky: true
                                    },
                                    {
                                        xtype: 'uni-search-internal-criteriapanel',
                                        itemId: 'search-criteria-removable',
                                        layout: 'column',
                                        margin: '0 0 0 100',
                                        service: me.getService()
                                    }
                                ],
                                bbar: {
                                    xtype: 'container',
                                    layout: {
                                        type: 'hbox',
                                        pack: 'end'
                                    },
                                    items: [
                                        {
                                            xtype: 'button',
                                            itemId: 'search-button',
                                            ui: 'action',
                                            text: Uni.I18n.translate('general.apply', 'MDC', 'Apply'),
                                            action: 'search'
                                        },
                                        {
                                            xtype: 'button',
                                            itemId: 'clear-all-button',
                                            text: Uni.I18n.translate('general.clearAll', 'MDC', 'Clear all'),
                                            action: 'clearFilters',
                                            margin: '0 0 0 0',
                                            disabled: true
                                        }
                                    ]
                                }
                            }
                        ]
                    }
                ]
            }
        ];

        me.callParent(arguments);

        var panel = me.down('#search-main-container');
        var listeners = store.on({
            beforeload:  function() {
                panel.setLoading(true);
            },
            load: function() {
                panel.setLoading(false);
            },
            scope: me,
            destroyable: true
        });

        var domainsListeners = domainsStore.on({
            load: function() {
                var visible = domainsStore.count() > 1;
                me.down('#search-domain').setVisible(visible);
                me.down('#search-domain-separator').setVisible(visible);
            },
            scope: me,
            destroyable: true
        });

        var resultsListeners = me.service.getSearchResultsStore().on({
            load: me.setGridMaxHeight,
            scope: me,
            destroyable: true
        });

        me.on('destroy', function () {
            listeners.destroy();
            resultsListeners.destroy();
            domainsListeners.destroy();
        });

        // me.callParent(arguments);
        //
        // var panel = me.down('#grid-filter');
        // var listeners = store.on({
        //     beforeload: function () {
        //         panel.setLoading(true);
        //     },
        //     load: function () {
        //         panel.setLoading(false);
        //     },
        //     scope: me,
        //     destroyable: true
        // });
        //
        // me.on('render', function () {
        //     me.down('#search-criteria-selector').setSearchContainer(Ext.ComponentQuery.query('contentcontainer')[0]);
        // });
        // me.on('destroy', function () {
        //     listeners.destroy();
        // });

    }
});