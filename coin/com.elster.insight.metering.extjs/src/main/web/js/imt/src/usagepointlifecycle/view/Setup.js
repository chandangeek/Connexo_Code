/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointlifecycle.view.Setup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.usagepoint-life-cycles-setup',
    xtype: 'usagepoint-life-cycles-setup',
    requires: [
        'Uni.view.notifications.NoItemsFoundPanel',
        'Imt.usagepointlifecycle.view.Grid',
        'Imt.usagepointlifecycle.view.Preview'
    ],

    router: null,

    initComponent: function () {
        var me = this;

        me.content = {
            ui: 'large',
            title: Uni.I18n.translate('general.usagePointLifeCycles', 'IMT', 'Usage point life cycles'),
            items: [
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'usagepoint-life-cycles-grid',
                        itemId: 'usagepoint-life-cycles-grid',
                        router: me.router
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        itemId: 'usagepoint-life-cycles-empty-panel',
                        title: Uni.I18n.translate('usagePointLifeCycles.empty.title', 'IMT', 'No usage point life cycles found'),
                        reasons: [
                            Uni.I18n.translate('usagePointLifeCycles.empty.list.item1', 'IMT', 'No usage point life cycles have been defined yet.'),
                            Uni.I18n.translate('usagePointLifeCycles.empty.list.item2', 'IMT', 'Usage point life cycles exist, but you do not have permission to view them.')
                        ],
                        stepItems: [
                            {
                                text: Uni.I18n.translate('general.addUsagePointLifeCycle', 'IMT', 'Add usage point life cycle'),
                                href: me.router.getRoute('administration/usagepointlifecycles/add').buildUrl(),
                                privileges: Imt.privileges.UsagePointLifeCycle.configure,
                                itemId: 'add-usagepoint-life-cycle-button'
                            }
                        ]
                    },
                    previewComponent: {
                        xtype: 'usagepoint-life-cycles-preview',
                        itemId: 'usagepoint-life-cycles-preview'
                    }
                }
            ]
        };

        me.callParent(arguments);
    }
});

