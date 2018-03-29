/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.view.customtask.log.Setup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.ctk-log-setup',
    requires: [
        'Uni.util.FormEmptyMessage',
        'Apr.view.customtask.log.Menu',
        'Apr.view.customtask.log.Grid',
        'Apr.view.customtask.log.Preview'
    ],
    task: null,
    runStartedOn: null,
    router: null,
    initComponent: function () {
        var me = this;

        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'ctk-log-menu',
                        itemId: 'log-view-menu',
                        router: me.router,
                        detailRoute: me.detailRoute,
                        objectType: me.objectType
                    }
                ]
            }
        ];


        me.content = {
            xtype: 'panel',
            itemId: 'main-panel',
            ui: 'large',
            title: Uni.I18n.translate('general.log', 'APR', 'Log'),
            items: [
                {
                    xtype: 'ctk-log-preview',
                    itemId: 'ctk-log-preview',
                    router: me.router,
                    taskId: me.task.get('id'),
                    margin: '10 0 20 0'
                },
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'ctk-log-grid',
                        itemId: 'ctk-log-grid'
                    },
                    emptyComponent: {
                        xtype: 'uni-form-empty-message',
                        doAutoSize: false,
                        text: Uni.I18n.translate('general.startedOnEmptyList', 'APR', '{0} started on {1} did not create any logs.', [me.task.get('name'), me.runStartedOn], false)
                    }
                }
            ]
        };

        me.callParent(arguments);
    }
});
