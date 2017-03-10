/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.view.validationtask.Preview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.cfg-tasks-preview',
    appName: null,

    requires: [
        'Cfg.view.validationtask.PreviewForm'
    ],

    tools: [
        {
            xtype: 'uni-button-action',
            menu: {
                xtype: 'cfg-validation-tasks-action-menu'
            }
        }
    ],

    initComponent: function () {
        var me = this;

        me.items = {
            xtype: 'cfg-tasks-preview-form',
            itemId: 'pnl-validation-task-preview-form',
            appName: me.appName
        };

        me.callParent(arguments);
    }
});
