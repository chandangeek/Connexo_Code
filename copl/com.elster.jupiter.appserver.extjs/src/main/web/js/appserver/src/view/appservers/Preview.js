/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.view.appservers.Preview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.appservers-preview',
    router: null,
    requires: [
        'Apr.view.appservers.PreviewForm',
        'Apr.view.appservers.Menu'
    ],

    initComponent: function () {
        var me = this;
        me.tools = [
            {
                xtype: 'uni-button-action',
                privileges: Apr.privileges.AppServer.admin,
                menu: {
                    xtype: 'appservers-action-menu'
                }
            }
        ];
        me.items = {
            xtype: 'appservers-preview-form',
            router: me.router
        };
        me.callParent(arguments);
    }

});

