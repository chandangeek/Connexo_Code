/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.view.appservers.MessageServicePreview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.msg-service-preview',
    requires: [
        'Apr.view.appservers.MessageServicePreviewForm',
        'Apr.view.appservers.MessageServicesActionMenu'
    ],

    initComponent: function () {
        var me = this;
        me.tools = [
            {
                xtype: 'uni-button-action',
                privileges: Apr.privileges.AppServer.admin,
                menu: {
                    xtype: 'message-services-action-menu'
                }
            }
        ];
        me.items = {
            xtype: 'msg-service-preview-form',
            router: me.router
        };
        me.callParent(arguments);
    }
});