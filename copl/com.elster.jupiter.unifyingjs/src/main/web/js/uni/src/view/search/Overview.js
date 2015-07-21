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
        'Uni.view.search.field.YesNo',
        'Uni.view.search.field.SearchObjectSelector',
        'Uni.view.search.field.SearchCriteriaSelector',
        'Uni.view.search.field.DateRangeField',
        'Uni.view.search.field.NumberRange'
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
                                items: [
                                    {

                                        xtype: 'label',
                                        text: Uni.I18n.translate('search.overview.searchDomains.label', 'UNI', 'Search for'),
                                        width: 100
                                    },
                                    {
                                        itemId: 'search-object-selector',
                                        xtype: 'search-object-selector'
                                    },
                                    '->',
                                    {
                                        disabled: true,
                                        xtype: 'search-criteria-selector'
                                    }
                                ]
                            },
                            {
                                // Sticky criteria.
                                itemId: 'search-criteria-sticky',
                                hidden: true,
                                items: [
                                    {
                                        xtype: 'label',
                                        text: Uni.I18n.translate('search.overview.criteria.label', 'UNI', 'Criteria'),
                                        width: 100
                                    },
                                    {
                                        flex: 1,
                                        xtype: 'container',
                                        itemId: 'criteria-sticky-items',
                                        //defaults: {
                                        //    margin: '10 10 10 0'
                                        //},
                                        layout: 'column'
                                    }
                                ]
                            },
                            {
                                // Sticky criteria.
                                itemId: 'search-criteria-removable',
                                hidden: true,
                                items: [
                                    {
                                        xtype: 'label',
                                        text: '',
                                        width: 100
                                    },
                                    {
                                        flex: 1,
                                        xtype: 'container',
                                        itemId: 'criteria-removable-items',
                                        //defaults: {
                                        //    margin: '10 10 10 0'
                                        //},
                                        layout: {
                                            type: 'column'
                                        }
                                    }
                                ]
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

                            // temp for testing date range field
/*                            {
                                xtype: 'uni-view-search-field-date-range'
                            },*/
                            //{
                            //    xtype: 'uni-view-search-field-number-range'
                            //},

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