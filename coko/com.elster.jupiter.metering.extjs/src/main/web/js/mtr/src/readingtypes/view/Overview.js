/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mtr.readingtypes.view.Overview', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.reading-types-setup',
    itemId: 'reading-types-setup',

    requires: [
        'Uni.view.notifications.NoItemsFoundPanel',
        'Uni.view.panel.FilterToolbar',
        'Mtr.readingtypes.view.Preview',
        'Mtr.readingtypes.view.Grid',
        'Mtr.readingtypes.view.SortingMenu',
        'Mtr.readingtypes.view.ActionMenu',
        'Mtr.readingtypes.util.FilterTopPanel'
    ],

    initComponent: function () {
        var me = this;

        me.content = {
            xtype: 'panel',
            ui: 'large',
            dockedItems: [
                {
                    xtype: 'reading-types-filter-top-panel',
                    itemId: 'reading-types-filter-top-panel',
                    store: Ext.getStore('Mtr.readingtypes.store.ReadingTypes'),
                    dock: 'top'
                }
            ],
            title: Uni.I18n.translate('readingtypesmanagment.readingtypes', 'MTR', 'Reading types'),
            items: [
                {
                    xtype: 'filter-toolbar',
                    title: Uni.I18n.translate('readingtypesmanagment.filter.sort', 'MTR', 'Sort'),
                    name: 'sortitemspanel',
                    itemId: 'reading-types-sorting-toolbar',
                    emptyText: Uni.I18n.translate('readingtypesmanagment.none','MTR','None'),
                    tools: [
                        {
                            xtype: 'button',
                            action: 'addSort',
                            text: Uni.I18n.translate('readingtypesmanagment.addSort', 'MTR', 'Add sort'),
                            menu: {
                                xtype: 'reading-types-sorting-menu',
                                itemId: 'reading-types-sorting-menu',
                                name: 'addsortitemmenu'
                            }
                        }
                    ]
                },

                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'metering-reading-types-grid',
                        router: me.router
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        itemId: 'no-load-profile',
                        title: Uni.I18n.translate('readingtypesmanagment.empty.title', 'MTR', 'No reading types found'),
                        reasons: [
                            Uni.I18n.translate('readingtypesmanagment.empty.list.noReadingTypes', 'MTR', 'No reading types added yet.'),
                            Uni.I18n.translate('readingtypesmanagment.empty.list.filter', 'MTR', 'No reading types comply with the filter.')
                        ],
                        stepItems: [
                            {
                                xtype:'button',
                                itemId:'overview-add-reading-type-button',
                                text:Uni.I18n.translate('readingtypesmanagment.pagingtoolbartop.addButton', 'MTR', 'Add reading types')
                            }
                        ]
                    },
                    previewComponent: {
                        xtype: 'reading-types-preview',
                        router: me.router
                    }
                }
            ]
        };

        me.callParent(arguments);
    }
});