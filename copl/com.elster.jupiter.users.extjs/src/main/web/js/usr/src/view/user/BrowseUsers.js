/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.view.search.Overview
 */
Ext.define('Usr.view.user.BrowseUsers', {

    extend: 'Ext.container.Container',

    xtype: 'usr-search-overview',

    itemId: 'centerContainer',

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
        'Uni.view.search.field.HasString',
        'Uni.view.search.field.Expiration',
        'Usr.view.user.Details',
        'Usr.view.user.UserActionMenu',
        'Uni.grid.column.Action'
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
                title: Uni.I18n.translate('search.overview.title', 'USR', 'Search'),
                cls: 'usr-search-overview',
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
                            align: 'stretch'
                        },
                        defaults: {
                            xtype: 'panel',
                            layout: 'hbox'
                        },
                        items: [
                            {
                                // Sticky criteria.
                                xtype: 'uni-search-internal-criteriapanel',
                                itemId: 'search-criteria-sticky',
                                layout: 'column',
                                lbar: {
                                    xtype: 'label',
                                    text: Uni.I18n.translate('search.overview.criteria.label', 'USR', 'Criteria'),
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
                                        xtype: 'combobox',
                                        fieldLabel: Uni.I18n.translate('general.load', 'USR', 'Load'),
                                        itemId: 'load-button',
                                        labelAlign: 'left',
                                        labelWidth: 'auto',
                                        id: 'loadDropDown',
                                        emptyText: Uni.I18n.translate('general.selectValue', 'USR', 'Select a value ...'),
                                        queryMode: 'local',
                                        style: {
                                            'margin-right': '25px'
                                        },
                                        store: Ext.create('Uni.store.search.SaveLoad'),
                                        displayField: 'name',
                                        displayValue: 'criteria',
                                        action: 'loadSearch',
                                        listConfig: {
                                            minWidth: 140,
                                            maxHeight: 250,
                                            style: "border-radius : 4px",
                                            shadow: true,
                                            bodyPadding: 10,
                                            margin: 0,
                                            getInnerTpl: function (displayField) {
                                                return '<a id="Remove-Icon" class="icon-cancel-circle2" style="float:right;cursor:default; display:inline-block; font-size:16px; margin-top:2px"></a>{' + displayField + '}';
                                            },
                                            listeners: {
                                                el: {
                                                    delegate: '.icon-cancel-circle2',
                                                    click: function (list, record, item, index, e) {
                                                        var cmp = Ext.ComponentQuery.query('#load-button')[0];
                                                        cmp.nameValue = "delete";
                                                        cmp.fireEvent('select', cmp);
                                                    }
                                                }
                                            }
                                        }
                                    },
                                    {
                                        xtype: 'tbspacer',
                                        width: 5
                                    },
                                    {
                                        xtype: 'button',
                                        itemId: 'save-search-button',
                                        id: 'saveSearchButton',
                                        text: Uni.I18n.translate('general.save', 'USR', 'Save'),
                                        action: 'saveSearchWindow',
                                        disabled: true

                                    },
                                    {
                                        xtype: 'button',
                                        itemId: 'search-button',
                                        ui: 'action',
                                        text: Uni.I18n.translate('general.search', 'USR', 'Search'),
                                        action: 'search'
                                    },
                                    {
                                        xtype: 'button',
                                        itemId: 'clear-all-button',
                                        text: Uni.I18n.translate('general.clearFilters', 'USR', 'Clear all'),
                                        action: 'clearFilters',
                                        margin: '0 0 0 0',
                                        disabled: true
                                    }
                                ]
                            }
                        ]
                    },
                    {
                        xtype: 'preview-container',
                        itemId: 'search-preview-container',
                        grid: {
                            xtype: 'uni-view-search-results',
                            service: me.getService(),
                            viewConfig: {
                                markDirty: false,
                            }
                        },
                        emptyComponent: {
                            itemId: 'search-no-items-found-panel',
                            xtype: 'no-items-found-panel',
                            title: Uni.I18n.translate('search.overview.noItemsFoundPanel.title', 'USR', 'No search results found'),
                            reasons: [
                                Uni.I18n.translate('search.overview.noItemsFoundPanel.item1', 'USR', 'No search criteria have been specified.'),
                                Uni.I18n.translate('search.overview.noItemsFoundPanel.item2', 'USR', 'There are no requested items.'),
                                Uni.I18n.translate('search.overview.noItemsFoundPanel.item3', 'USR', 'No search results comply with the filter.')
                            ],
                            margin: '16 0 0 0'
                        },
                        previewComponent: {
                            xtype: 'userDetails'
                        }
                    }
                ]
            }
        ];

        this.callParent(arguments);

        var panel = me.down('#search-main-container');
        var listeners = store.on({
            beforeload: function () {
                panel.setLoading(true);
            },
            load: function () {
                panel.setLoading(false);
            },
            scope: me,
            destroyable: true
        });

        var domainListeners = me.service.getSearchDomainsStore().on({
            load: function () {
                // Added in order to get rid of 'save/load criteria filter' functionallity
                // As soon as this will be redone on another layer of abstraction, this can be deleted.
                me.down('#load-button').setVisible(false);
                me.down('#save-search-button').setVisible(false);
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
            domainListeners.destroy();
            resultsListeners.destroy();
        });
    },

    setGridMaxHeight: function () {
        var me = this,
            grid = me.down('uni-view-search-results'),
            panel = me.down('panel'),
            pageHeight = me.getHeight() - panel.getHeader().getHeight() - 40,
            filterHeight = me.down('#search-main-container').getHeight();

        if (pageHeight - filterHeight > 450) {
            grid.maxHeight = pageHeight - filterHeight;
        } else {
            grid.maxHeight = 450;
        }
        grid.updateLayout();
    }
});