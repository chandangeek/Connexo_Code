/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dxp.view.tasks.HistoryPreview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.dxp-tasks-history-preview',

    requires: [
        'Dxp.view.tasks.HistoryPreviewForm'
    ],

    initComponent: function () {
        var me = this;

        me.tools = [
            {
                xtype: 'uni-button-action',
                itemId: 'history-preview-actions-button',
                privileges: Dxp.privileges.DataExport.run,
                menu: {
                    xtype: 'history-grid-action-menu',
                    itemId: 'history-grid-action-menu',
                    router: me.router
                },
                listeners: {
                    click: function () {
                        this.showMenu();
                    }
                }
            }
        ];
        me.items = {
            xtype: 'dxp-tasks-history-preview-form'
        };
        me.callParent();
    }
});

