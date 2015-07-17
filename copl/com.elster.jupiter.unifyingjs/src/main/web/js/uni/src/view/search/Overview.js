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
        'Uni.view.search.field.AddCriteriaButton',
        'Uni.view.search.field.DateRangeField'
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
                            type: 'table',
                            columns: 3,
                            tableAttrs: {
                                cls: 'search-table'
                            }
                        },
                        defaults: {
                            cellCls: 'search-cell'
                        },
                        items: [
                            // Type of search.
                            {
                                cellCls: 'search-cell search-label',
                                xtype: 'label',
                                text: Uni.I18n.translate('search.overview.searchDomains.label', 'UNI', 'Search for')
                            },
                            {
                                colspan: 2,
                                xtype: 'uni-view-search-field-search-object-selector'
                            },
                            // Sticky criteria.
                            {
                                cellCls: 'search-cell search-label',
                                xtype: 'label',
                                text: Uni.I18n.translate('search.overview.criteria.label', 'UNI', 'Criteria')

                            },
                            // temp for testing date range field
                            {
                                xtype: 'uni-view-search-field-date-range'
                            },
                            {
                                xtype: 'container',
                                itemId: 'stickycriteria',
                                defaults: {
                                    margin: '10 10 10 0'
                                },
                                layout: {
                                    type: 'column'
                                }
                            },
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
                            {
                                cellCls: 'search-cell search-last',
                                margin: '0 2 0 0',
                                xtype: 'uni-view-search-field-add-criteria-button'
                            },
                            // Extra criteria.
                            {
                                html: '&nbsp;',
                                itemId: 'removablecriteriaplaceholder'
                            },
                            {
                                colspan: 2,
                                xtype: 'container',
                                itemId: 'removablecriteria',
                                defaults: {
                                    margin: '0 10 10 0'
                                },
                                layout: {
                                    type: 'column'
                                }
                            },
                            // Filter controls.
                            {
                                colspan: 3,
                                xtype: 'container',
                                layout: {
                                    type: 'hbox'
                                },
                                items: [
                                    {
                                        flex: 1
                                    },
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
                            // Sorting. (Not in scope at this moment of writing, removing breaks the column layout so just disabling the xtypes.)

                            {
                                cellCls: 'search-cell search-label',
                                //xtype: 'label',
                                text: 'Sort'
                            },
                            {
                                //hidden: true,
                                //xtype: 'container',
                                layout: {
                                    type: 'hbox'
                                },
                                items: [
                                    {
                                        //xtype: 'combo',
                                        emptyText: Uni.I18n.translate('general.addSort', 'UNI', 'Add sort')
                                    }
                                ]
                            },
                            {
                                cellCls: 'search-cell search-last',
                                //xtype: 'container',
                                layout: {
                                    type: 'hbox'
                                },
                                items: [
                                    {
                                        flex: 1
                                    },
                                    {
                                        //xtype: 'button',
                                        text: Uni.I18n.translate('general.clearSorting', 'UNI', 'Clear sorting'),
                                        action: 'clearSorting',
                                        margin: '0 0 0 0'
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