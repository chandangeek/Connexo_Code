/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dxp.view.log.Setup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.log-setup',
    requires: [
        'Uni.util.FormEmptyMessage',
        'Dxp.view.log.Menu',
        'Dxp.view.log.Grid',
        'Dxp.view.log.Preview'
    ],
    task: null,
    runStartedOn: null,
    fromWorkspace: false,
    router: null,
    initComponent: function () {
        var me = this;

        if(!me.fromWorkspace){
            me.side = [
                {
                    xtype: 'panel',
                    ui: 'medium',
                    items: [
                        {
                            xtype: 'dxp-log-menu',
                            itemId: 'log-view-menu',
                            router: me.router
                        }
                    ]
                }
            ];
        }

        me.content = {
            xtype: 'panel',
            itemId: 'main-panel',
            ui: 'large',
            title: Uni.I18n.translate('general.log', 'DES', 'Log'),
            items: [
                {
                    xtype: 'dxp-log-preview',
                    router: me.router,
                    taskId: me.task.get('id'),
                    margin: '10 0 20 0'
                },
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'dxp-log-grid',
                        itemId: 'data-export-log-grid'
                    },
                    emptyComponent: {
                        xtype: 'uni-form-empty-message',
                        text: Uni.I18n.translate('general.startedOnEmptyList', 'DES', '{0} started on {1} did not create any logs.',[me.task.get('name'),me.runStartedOn])
                    }
                }
            ]
        };

        me.callParent(arguments);
    }
});
