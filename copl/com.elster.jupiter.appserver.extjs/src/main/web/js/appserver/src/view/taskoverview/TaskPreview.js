/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.view.taskoverview.TaskPreview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.task-preview',
    router: null,
    requires: [],

    initComponent: function () {
        var me = this;
        me.tools = [
            {
                xtype: 'uni-button-action',
                itemId: 'task-button-action',
                privileges: Apr.privileges.AppServer.admin,
                menu: {
                    xtype: 'task-overview-action-menu'
                }
            }
        ];
        me.items = {
            xtype: 'task-preview-form',
            itemId: 'task-preview-form'
        };
        me.callParent(arguments);
    }

});
