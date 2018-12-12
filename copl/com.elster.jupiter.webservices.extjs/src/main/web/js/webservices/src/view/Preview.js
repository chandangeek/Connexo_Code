/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Wss.view.Preview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.webservices-preview',
    requires: [
        'Wss.view.PreviewForm',
        'Wss.view.ActionMenu'
    ],

    initComponent: function () {
        var me = this;
        me.tools = [
            {
                xtype: 'uni-button-action',
                itemId: 'webservicePreviewMenuButton',
                privileges: Wss.privileges.Webservices.admin,
                menu: {
                    xtype: 'webservices-action-menu'
                }
            }
        ];

        me.items = {
            xtype: 'webservices-preview-form',
            itemId: 'webservices-grid-preview-form'
        };
        me.callParent(arguments);
    }

});