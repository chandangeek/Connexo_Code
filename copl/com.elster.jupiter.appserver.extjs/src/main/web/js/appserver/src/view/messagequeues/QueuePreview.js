/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.view.messagequeues.QueuePreview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.queue-preview',
    router: null,
    requires: [],

    initComponent: function () {
        var me = this;
        me.tools = [
            {
                xtype: 'uni-button-action',
                itemId: 'queue-button-action',
                privileges: Apr.privileges.AppServer.admin,
                menu: {
                    xtype: 'queue-action-menu'
                }
            }
        ];
        me.items = {
            xtype: 'queue-preview-form',
            itemId: 'queue-preview-form'
        };
        me.callParent(arguments);
    }

});


