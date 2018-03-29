/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.view.customtask.Details', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.ctk-task-details',
    requires: [
        'Apr.view.customtask.Menu',
        'Apr.view.customtask.PreviewForm'
    ],

    router: null,
    taskId: null,
    appName: null,

    initComponent: function () {
        var me = this;

        me.content = {
            xtype: 'container',
            layout: 'hbox',
            items: [
                {
                    ui: 'large',
                    itemId: 'ctk-task-details-panel',
                    title: Uni.I18n.translate('general.details', 'APR', 'Details'),
                    flex: 1,
                    objectType: me.objectType,
                    items: {
                        xtype: 'ctk-task-preview-form',
                        itemId: 'frm-task-details',
                        margin: '0 0 0 100',
                        appName: me.appName
                    }
                },
                {
                    xtype: 'uni-button-action',
                    privileges: function () {
                        return me.canAdministrate;
                    },
                    margin: '20 0 0 0',
                    menu: me.actionMenu
                }
            ]
        };


        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'ctk-tasks-menu',
                        itemId: 'tasks-view-menu',
                        router: me.router,
                        taskId: me.taskId,
                        detailRoute: me.detailRoute,
                        historyRoute: me.historyRoute,
                        canHistory: me.canHistory,
                        objectType: me.objectType
                    }
                ]
            }
        ];

        this.callParent(arguments);
    }
});

