/**
 * @class Uni.view.search.Overview
 */
Ext.define('Uni.view.search.Overview', {
    extend: 'Ext.container.Container',
    xtype: 'uni-view-search-overview',
    overflowY: 'auto',

    requires: [
        'Uni.view.container.PreviewContainer',
        'Uni.view.notifications.NoItemsFoundPanel',
        'Uni.view.search.Results',
        'Uni.store.search.Domains',
        'Uni.store.search.Removables',
        'Uni.view.search.field.Boolean',
        'Uni.view.search.field.SearchObjectSelector',
        'Uni.view.search.field.SearchCriteriaSelector',
        'Uni.view.search.field.DateTime',
        'Uni.view.search.field.Numeric',
        'Uni.view.search.field.Selection',
        'Uni.view.search.field.Simple'
    ],

    padding: '16 16 16 16',

    initComponent: function () {
        var me = this;

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
                                        xtype: 'search-object-selector'
                                    },
                                    {
                                        xtype: 'uni-search-criteria-boolean',
                                        text: 'Boolean'
                                    },
                                    '->',
                                    {
                                        disabled: true,
                                        xtype: 'search-criteria-selector',
                                        margin: 0
                                    }
                                ]
                            },
                            {
                                xtype: 'menuseparator'
                            },
                            {
                                // Sticky criteria.
                                xtype: 'panel',
                                itemId: 'search-criteria-sticky',
                                hidden: true,
                                lbar: {
                                    xtype: 'label',
                                    text: Uni.I18n.translate('search.overview.criteria.label', 'UNI', 'Criteria'),
                                    width: 100
                                },
                                flex: 1,
                                defaults: {
                                    margin: '0 10 10 0'
                                },
                                margin: '10 0 0 0',
                                layout: 'column'
                            },
                            {
                                // Removable criteria.
                                xtype: 'panel',
                                itemId: 'search-criteria-removable',
                                hidden: true,
                                lbar: {
                                    xtype: 'label',
                                    text: '',
                                    width: 100
                                },
                                flex: 1,
                                defaults: {
                                    margin: '0 10 10 0'
                                },
                                layout: 'column'
                            },
                            {
                                xtype: 'toolbar',
                                items: [
                                    '->',
                                    {
                                        xtype: 'button',
                                        ui: 'action',
                                        text: Uni.I18n.translate('general.search', 'UNI', 'Search'),
                                        action: 'search'
                                    },
                                    {
                                        xtype: 'button',
                                        text: Uni.I18n.translate('general.clearFilters', 'UNI', 'Clear all'),
                                        action: 'clearFilters',
                                        margin: '0 0 0 0'
                                    }
                                ]
                            },
                            {
                                xtype: 'menuseparator'
                            },
                            {
                                // Sorting. (Not in scope at this moment of writing, removing breaks the column layout so just disabling the xtypes.)
                                xtype: 'toolbar',
                                margin: 0,
                                defaults: {
                                    disabled: true
                                },
                                items: [
                                    {
                                        xtype: 'label',
                                        text: 'Sort',
                                        width: 100
                                    },
                                    {
                                        text: Uni.I18n.translate('general.addSort', 'UNI', 'Add sort')
                                    },
                                    '->',
                                    {
                                        text: Uni.I18n.translate('general.clearSorting', 'UNI', 'Clear sorting'),
                                        action: 'clearSorting'
                                    }
                                ]
                            }
                        ]
                    },
                    {
                        xtype: 'preview-container',
                        grid: {
                            xtype: 'uni-view-search-results'
                        },
                        emptyComponent: {
                            xtype: 'no-items-found-panel',
                            title: Uni.I18n.translate('search.overview.noItemsFoundPanel.title', 'UNI', 'No search results found'),
                            reasons: [
                                Uni.I18n.translate('search.overview.noItemsFoundPanel.item1', 'UNI', 'No filters have been applied.'),
                                Uni.I18n.translate('search.overview.noItemsFoundPanel.item2', 'UNI', 'There are no requested items.'),
                                Uni.I18n.translate('search.overview.noItemsFoundPanel.item3', 'UNI', 'The filter is too narrow.')
                            ],
                            margins: '16 0 0 0'
                        },
                        previewComponent: {
                            hidden: true
                        }
                    }
                ]
            }
        ];
        this.callParent(arguments);
    }
});