/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Est.estimationtasks.view.Log', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.estimationtasks-log-setup',
    requires: [
        'Uni.util.FormEmptyMessage',
        'Est.estimationtasks.view.LogGrid',
        'Est.estimationtasks.view.LogPreview',
        'Est.estimationtasks.view.LogSideMenu'
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
                        xtype: 'estimationtasks-log-menu',
                        itemId: 'estimationtasks-log-menu',
                        taskId: me.taskId,
                        router: me.router,
                        occurence: me.occurenceId,
                        detailLogRoute: me.detailLogRoute,
                        logRoute: me.logRoute
                    }
                ]
            }
        ];
        me.content = {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('estimationtasks.general.log', 'EST', 'Log'),
            items: [
                {
                    xtype: 'estimationtasks-log-preview',
                    router: me.router,
                    margin: '10 0 20 0'
                },
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'estimationtasks-log-grid'
                    },
                    emptyComponent: {
                        xtype: 'uni-form-empty-message',
                        text: Ext.String.format(
                            Uni.I18n.translate('estimationtasks.log.empty.list.item1', 'EST', "Estimation task '{0}' started on {1} did not create any logs."),
                            [me.task.get('name'), me.runStartedOn], false)
                    }
                }
            ]
        };

        me.callParent(arguments);
    }
});