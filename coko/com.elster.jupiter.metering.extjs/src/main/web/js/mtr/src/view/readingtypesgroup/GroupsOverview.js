/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mtr.view.readingtypesgroup.GroupsOverview', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.reading-type-groups-overview',

    requires: [
        'Uni.view.notifications.NoItemsFoundPanel',
        'Mtr.view.readingtypesgroup.GroupPreview',
        'Mtr.view.readingtypesgroup.GroupsGrid'
    ],

    router : null,

    initComponent: function () {
        var me = this;

        me.content = {
            xtype: 'panel',
            ui: 'large',
            //title: Uni.I18n.translate('readingtypes.readingTypeGroups', 'MTR', 'Reading type groups'), // lori set
            title: Uni.I18n.translate('readingtypes.readingTypes1', 'MTR', 'Reading types'),
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
                        title: Uni.I18n.translate('readingtypesmanagement.readingtypes.empty.title', 'MTR', 'No reading types found'), //lori set
                        reasons: [
                            Uni.I18n.translate('readingtypesmanagement.readingtypes.empty.list.noReadingTypes', 'MTR', 'No reading types added yet.'),
                            Uni.I18n.translate('readingtypesmanagement.empty.list.filter', 'MTR', 'No reading types comply with the filter.')
                        ],
                        stepItems: [
                            {
                                xtype:'button',
                                itemId:'mtr-reading-type-groups-overview-add-button',
                                text: Uni.I18n.translate('readingtypesmanagement.pagingtoolbartop.addButton', 'MTR', 'Add reading type PPP')  //lori set
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
