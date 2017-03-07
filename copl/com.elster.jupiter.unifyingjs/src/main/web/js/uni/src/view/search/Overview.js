/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.view.search.Overview
 */
Ext.define('Uni.view.search.Overview', {
    extend: 'Ext.container.Container',
    xtype: 'uni-view-search-overview',
    itemId: 'centerContainer', // added for test
    overflowY: 'auto',

    requires: [
        'Uni.view.container.PreviewContainer',
        'Uni.view.container.EmptyGridContainer',
        'Uni.view.notifications.NoItemsFoundPanel',
        'Uni.view.search.Results',
        'Uni.store.search.Domains',
        'Uni.store.search.Removables',
        'Uni.view.search.field.internal.CriteriaPanel',
        'Uni.view.search.field.Boolean',
        'Uni.view.search.field.SearchObjectSelector',
        'Uni.view.search.field.SearchCriteriaSelector',
        'Uni.view.search.field.DateTime',
        'Uni.view.search.field.Numeric',
        'Uni.view.search.field.Selection',
        'Uni.view.search.field.Simple',
        'Uni.view.search.field.Obis',
        'Uni.view.search.field.TimeDuration',
        'Uni.view.search.field.Date',
        'Uni.view.search.field.Clock',
        'Uni.view.search.field.TimeOfDay',
        'Uni.view.search.field.Quantity',
        'Uni.view.search.field.Location',
        'Uni.view.search.field.HasString'
    ],

    padding: '16 16 16 16',

    config: {
        service: null
    },

    initComponent: function () {
        var me = this,
            domainsStore = this.getService().getSearchDomainsStore(),
            store = Ext.getStore('Uni.store.search.Properties');

        me.items = [
            {
                xtype: 'panel',
                title: Uni.I18n.translate('search.overview.title', 'UNI', 'Search'),
                cls: 'uni-view-search-overview',
                ui: 'large',
                layout: {
                    type: 'vbox',
                    align: 'stretch'
                },
                items: [
                    {
                        xtype: 'panel',
                        itemId: 'search-main-container',
                        ui: 'filter',
                        layout: {
                            type: 'vbox',
                            align : 'stretch'
                        },
                        defaults: {
                            xtype: 'panel',
                            layout: 'hbox'
                        },
                        items: [
                            {
                                // Type of search.
                                xtype: 'toolbar',
                                hidden: true,
                                itemId: 'search-domain',
                                defaults: {
                                    margin: '0 10 10 0'
                                },
                                padding: 0,
                                items: [
                                    {
                                        xtype: 'label',
                                        text: Uni.I18n.translate('search.overview.searchDomains.label', 'UNI', 'Search for'),
                                        width: 100,
                                        margin: 0
                                    },
                                    {
                                        itemId: 'search-object-selector',
                                        xtype: 'search-object-selector',
                                        service: me.getService()
                                    }
                                ]
                            },
                            {
                                hidden: true,
                                itemId: 'search-domain-separator',
                                xtype: 'menuseparator'
                            },
                            {
                                // Sticky criteria.
                                xtype: 'uni-search-internal-criteriapanel',
                                itemId: 'search-criteria-sticky',
                                layout: 'column',
                                lbar: {
                                    xtype: 'label',
                                    text: Uni.I18n.translate('search.overview.criteria.label', 'UNI', 'Criteria'),
                                    width: 100
                                },
                                rbar: [{
                                    disabled: true,
                                    itemId: 'search-criteria-selector',
                                    xtype: 'search-criteria-selector',
                                    margin: 0,
                                    searchContainer: me,
                                    service: me.getService()
                                }],
                                margin: '10 0 0 0',
                                service: me.getService(),
                                sticky: true
                            },
                            {
                                // Removable criteria.
                                xtype: 'uni-search-internal-criteriapanel',
                                itemId: 'search-criteria-removable',
                                layout: 'column',
                                margin: '0 0 0 100',
                                service: me.getService()
                            },
                            {
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
                                        text: Uni.I18n.translate('general.search', 'UNI', 'Search'),
                                        action: 'search'
                                    },
                                    {
                                        xtype: 'button',
                                        itemId: 'clear-all-button',
                                        text: Uni.I18n.translate('general.clearFilters', 'UNI', 'Clear all'),
                                        action: 'clearFilters',
                                        margin: '0 0 0 0',
                                        disabled: true
                                    }
                                ]
                            }
                        ]
                    },
                    {
                        xtype: 'emptygridcontainer',
                        itemId: 'search-preview-container',
                        grid: {
                            xtype: 'uni-view-search-results',
                            service: me.getService()
                        },
                        emptyComponent: {
                            itemId: 'search-no-items-found-panel',
                            xtype: 'no-items-found-panel',
                            title: Uni.I18n.translate('search.overview.noItemsFoundPanel.title', 'UNI', 'No search results found'),
                            reasons: [
                                Uni.I18n.translate('search.overview.noItemsFoundPanel.item1', 'UNI', 'No search criteria have been specified.'),
                                Uni.I18n.translate('search.overview.noItemsFoundPanel.item2', 'UNI', 'There are no requested items.'),
                                Uni.I18n.translate('search.overview.noItemsFoundPanel.item3', 'UNI', 'No search results comply with the filter.')
                            ],
                            margin: '16 0 0 0'
                        }
                    }
                ]
            }
        ];

        this.callParent(arguments);

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
    },

    setGridMaxHeight: function () {
        var me = this,
            grid = me.down('uni-view-search-results'),
            panel = me.down('panel'),
            pageHeight = me.getHeight() - panel.getHeader().getHeight() - 40,
            filterHeight = me.down('#search-main-container').getHeight();

        me.down('#search-preview-container').getEmptyCt().margins = '16 0 0 0';
        if (pageHeight - filterHeight > 450) {
            grid.maxHeight = pageHeight - filterHeight;
        } else {
            grid.maxHeight = 450;
        }
        grid.updateLayout();
    }
});