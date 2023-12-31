/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dxp.view.tasks.Details', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.data-export-tasks-details',
    requires: [
        'Dxp.view.tasks.Menu',
        'Dxp.view.tasks.PreviewForm',
        'Dxp.view.tasks.ActionMenu'
    ],

    router: null,
    taskId: null,

    content: {
        xtype: 'container',
        layout: 'hbox',
        items: [
            {
                ui: 'large',
                itemId: 'tasks-details-panel',
                title: Uni.I18n.translate('general.details', 'DES', 'Details'),
                flex: 1,
                items: {
                    xtype: 'dxp-tasks-preview-form',
                    margin: '0 0 0 100',
                    maxWidth: 600
                }
            },
            {
                xtype: 'uni-button-action',
                margin: '20 0 0 0',
                menu: {
                    xtype: 'dxp-tasks-action-menu'
                }
            }
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
                        xtype: 'dxp-tasks-menu',
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

