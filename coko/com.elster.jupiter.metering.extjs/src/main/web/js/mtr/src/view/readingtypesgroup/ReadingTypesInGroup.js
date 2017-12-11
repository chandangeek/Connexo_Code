/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mtr.view.readingtypesgroup.ReadingTypesInGroup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.reading-types-in-group',
    requires: [
        'Mtr.view.readingtypesgroup.Menu',
        'Uni.view.notifications.NoItemsFoundPanel',
        'Uni.view.panel.FilterToolbar',
        'Mtr.view.readingtypes.Preview',
        'Mtr.view.readingtypesgroup.ReadingTypesGrid',
        'Mtr.view.readingtypesgroup.GroupActionMenu',
        'Mtr.util.readingtypesgroup.FilterTopPanel'
    ],

    router: null,

    content: {
        xtype: 'container',
        layout: 'hbox',
        items: [

        ]
    },

    initComponent: function () {
        var me = this;
        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'reading-types-group-menu',
                        itemId: 'mnu-reading-types-group',
                        router: me.router,
                        toggle: 0
                    }
                ]
            }
        ];

        me.content = {
            xtype: 'panel',
            itemid : 'reading-types-in-group-form',
            ui: 'large',
            dockedItems: [
                {
                    xtype: 'reading-types-group-filter-top-panel',
                    itemId: 'reading-types-group-filter-top-panel',
                    store: Ext.getStore('Mtr.store.readingtypesgroup.ReadingTypesByAlias'),
                    dock: 'top'
                }
            ],
            title: Uni.I18n.translate('general.readingTypes', 'MTR', 'Reading Types'),
            items: [
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'reading-types-in-group-grid',
                        router: me.router
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        itemId: 'reading-types-empty-grid',
                        title: Uni.I18n.translate('readingtypesmanagement.readingtypes.empty.title', 'MTR', 'No reading types found'),
                        reasons: [
                            Uni.I18n.translate('readingtypesmanagement.readingtypes.empty.list.item1', 'MTR', 'There are no reading types in this group.'),
                            Uni.I18n.translate('readingtypesmanagement.readingtypes.empty.list.item2', 'MTR', 'No reading types comply with the filter.')
                        ]
                    },
                    previewComponent: {
                        //xtype: 'fim-history-preview',
                        //itemId: 'pnl-history-preview',
                        xtype: 'reading-types-preview',
                        router: me.router
                    }
                }
            ]
        };

        this.callParent(arguments);
    }
});


