/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mtr.readingtypes.view.GroupsOverview', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.reading-type-groups-overview',

    requires: [
        'Uni.view.notifications.NoItemsFoundPanel',
        'Mtr.readingtypes.view.GroupPreview',
        'Mtr.readingtypes.view.GroupsGrid',
        'Mtr.readingtypes.view.GroupActionMenu'
        // 'Mtr.readingtypes.util.FilterTopPanel'
    ],

    initComponent: function () {
        var me = this;

        me.content = {
            xtype: 'panel',
            ui: 'large',
            // dockedItems: [
            //     {
            //         xtype: 'reading-types-filter-top-panel',
            //         itemId: 'reading-types-filter-top-panel',
            //         store: Ext.getStore('Mtr.readingtypes.store.ReadingTypes'),
            //         dock: 'top'
            //     }
            // ],
            title: Uni.I18n.translate('readingtypes.readingTypeGroups', 'MTR', 'Reading type groups'),
            items: [
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'reading-type-groups-grid',
                        router: me.router
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        itemId: 'mtr-noReadingTypeGroups',
                        title: Uni.I18n.translate('readingtypesmanagement.empty.title', 'MTR', 'No reading type groups found'),
                        reasons: [
                            Uni.I18n.translate('readingtypesmanagement.empty.list.noReadingTypes', 'MTR', 'No reading type groups added yet.'),
                            Uni.I18n.translate('readingtypesmanagement.empty.list.filter', 'MTR', 'No reading type groups comply with the filter.')
                        ],
                        stepItems: [
                            {
                                xtype:'button',
                                itemId:'mtr-reading-type-groups-overview-add-button',
                                text:Uni.I18n.translate('readingtypesmanagement.pagingtoolbartop.addButton', 'MTR', 'Add reading type group')
                            }
                        ]
                    },
                    previewComponent: {
                        xtype: 'readingTypesGroup-preview',
                        router: me.router
                    }
                }
            ]
        };

        me.callParent(arguments);
    }
});
