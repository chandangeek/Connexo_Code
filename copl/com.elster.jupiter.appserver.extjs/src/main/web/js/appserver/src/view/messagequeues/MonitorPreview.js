/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.view.messagequeues.MonitorPreview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.monitor-preview',
    router: null,
    requires: [
    ],

    initComponent: function () {
        var me = this;
        me.tools = [
            {
                xtype: 'uni-button-action',
                privileges: Apr.privileges.AppServer.admin,
                menu: {
                    xtype: 'monitor-action-menu'
                }
            }
        ];
        me.items = {
            xtype: 'monitor-preview-form'
        };
        me.callParent(arguments);
    }

});


