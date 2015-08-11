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
        'Uni.view.search.field.DateInterval',
        'Uni.view.search.field.Interval',
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
                                        xtype: 'search-combo',
                                        itemId: 'domain',
                                        store: Ext.create('Ext.data.Store', {
                                            fields: ['name', 'value'],
                                            data: [
                                                {'name': 'SPE010000010079', 'value': '1'},
                                                {'name': 'SPE010000010080', 'value': '2'},
                                                {'name': 'SPE010000010081', 'value': '1'},
                                                {'name': 'SPE010000010082', 'value': '2'},
                                                {'name': 'SPE010000010083', 'value': '1'},
                                                {'name': 'SPE010000010084', 'value': '2'},
                                                {'name': 'SPE010000010085', 'value': '1'},
                                                {'name': 'SPE010000010086', 'value': '2'},
                                                {'name': 'SPE010000010087', 'value': '1'},
                                                {'name': 'DME010000010088', 'value': '2'},
                                                {'name': 'DME010000010079', 'value': '1'},
                                                {'name': 'DME010000010080', 'value': '2'},
                                                {'name': 'DME010000010081', 'value': '1'},
                                                {'name': 'DME010000010082', 'value': '2'},
                                                {'name': 'DME010000010083', 'value': '1'},
                                                {'name': 'DME010000010084', 'value': '2'},
                                                {'name': 'DME010000010085', 'value': '1'},
                                                {'name': 'DME010000010086', 'value': '2'},
                                                {'name': 'DME010000010087', 'value': '1'},
                                                {'name': 'DME010000010088', 'value': '2'}
                                            ],
                                            limit: 10
                                        }),
                                        emptyText: 'mRID',
                                        displayField: 'name',
                                        valueField: 'id',
                                        margin: '0 20 10 0',
                                        forceSelection: true,
                                        multiSelect: true
                                    },
                                    // temp for testing date/number range field
                                    {
                                        xtype: 'uni-view-search-field-date-field',
                                        emptyText: 'DateTime'
                                    },
                                    {
                                        xtype: 'uni-view-search-field-number-field',
                                        emptyText: 'Interaval'
                                    },
                                    {
                                        xtype: 'uni-view-search-field-yesno',
                                        emptyText: 'Boolean'
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
                                        text: Uni.I18n.translate('general.clearFilters', 'UNI', 'Clear filters'),
                                        action: 'clearFilters',
                                        margin: '0 0 0 0'
                                    }
                                ]
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



/*                            { //leaved for integration default field
                                cellCls: 'search-cell search-last',
                                xtype: 'combo',
                                itemId: 'addcriteria',
                                emptyText: Uni.I18n.translate('search.overview.addCriteria.emptyText', 'UNI', 'Add criteria'),
                                margin: '0 2 0 0',
                                store: 'Uni.store.search.Removables',
                                displayField: 'displayValue',
                                valueField: 'name',
                                forceSelection: true,
                                queryMode: 'local',
                                multiSelect: false
                            },*/

                            // Extra criteria.
                            //{
                            //    html: '&nbsp;',
                            //    itemId: 'removablecriteriaplaceholder'
                            //},
                            //{
                            //    colspan: 2,
                            //    xtype: 'container',
                            //    itemId: 'removablecriteria',
                            //    defaults: {
                            //        margin: '0 10 10 0'
                            //    },
                            //    layout: {
                            //        type: 'column'
                            //    }
                            //},
                            // Filter controls.
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