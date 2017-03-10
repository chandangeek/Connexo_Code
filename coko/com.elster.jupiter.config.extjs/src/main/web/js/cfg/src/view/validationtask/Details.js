/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.view.validationtask.Details', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.cfg-validation-tasks-details',
    requires: [
        'Cfg.view.validationtask.Menu',
        'Cfg.view.validationtask.PreviewForm',
        'Cfg.view.validationtask.ActionMenu'
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
                    itemId: 'frm-validation-task-details-panel',
                    title: Uni.I18n.translate('general.details', 'CFG', 'Details'),
                    flex: 1,
                    items: {
                        xtype: 'cfg-tasks-preview-form',
                        itemId: 'frm-validation-task-details',
                        margin: '0 0 0 100',
                        appName: me.appName
                    }
                },
                {
                    xtype: 'uni-button-action',
                    margin: '20 0 0 0',
                    menu: {
                        xtype: 'cfg-validation-tasks-action-menu'
                    }
                }
            ]
        };

        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'cfg-tasks-menu',
                        itemId: 'tasks-view-menu',
                        router: me.router,
                        taskId: me.taskId
                    }
                ]
            }
        ];

        this.callParent(arguments);
    }
});

