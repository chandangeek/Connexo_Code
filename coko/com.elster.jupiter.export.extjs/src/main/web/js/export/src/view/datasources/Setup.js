/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dxp.view.datasources.Setup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.data-sources-setup',
    requires: [
        'Uni.view.notifications.NoItemsFoundPanel',
        'Dxp.view.tasks.Menu',
        'Dxp.view.datasources.Grid'
    ],
    taskId: null,
    router: null,
    initComponent: function () {
        var me = this;

        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'dxp-tasks-menu',
                        itemId: 'tasks-view-menu',
                        taskId: me.taskId,
                        router: me.router
                    }
                ]
            }
        ];

        me.content = {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('general.dataSources', 'DES', 'Data sources'),
            items: [
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'data-sources-grid',
                        router: me.router
                    },
                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        title: Uni.I18n.translate('dataSources.empty.title', 'DES', 'No data sources found'),
                        reasons: Uni.util.Application.getAppName() === 'MultiSense'
                            ? [
                            Uni.I18n.translate('dataSources.device.empty.list.item1', 'DES', 'The data export task has never run.'),
                            Uni.I18n.translate('dataSources.device.empty.list.item2', 'DES', 'The device group does not contain any devices.'),
                            Uni.I18n.translate('dataSources.device.empty.list.item3', 'DES', 'None of the devices in the group has a data source matching one of the selected reading types.')
                        ]
                            : [
                            Uni.I18n.translate('dataSources.usagePoint.empty.list.item1', 'DES', 'The data export task has never run.'),
                            Uni.I18n.translate('dataSources.usagePoint.empty.list.item2', 'DES', 'The usage point group does not contain any usage points.'),
                            Uni.I18n.translate('dataSources.usagePoint.empty.list.item3', 'DES', 'None of the usage points in the group has data source matching one of the selected reading types.')
                        ]
                    }
                }
            ]
        };

        me.callParent(arguments);
    }
});

