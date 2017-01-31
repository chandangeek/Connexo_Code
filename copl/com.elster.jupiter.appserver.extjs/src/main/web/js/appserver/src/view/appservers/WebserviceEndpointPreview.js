/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.view.appservers.WebserviceEndpointPreview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.webservice-preview',
    router: null,

    requires: [
        'Apr.view.appservers.WebserviceEndpointPreviewForm',
        'Apr.view.appservers.WebserviceEndpointActionMenu'
    ],

    initComponent: function () {
        var me = this;
        me.tools = [
            {
                xtype: 'uni-button-action',
                privileges: Apr.privileges.AppServer.admin,
                menu: {
                    xtype: 'apr-webservices-action-menu'
                }
            }
        ];
        me.items = {
            xtype: 'webservice-preview-form',
            router: me.router
        };
        me.callParent(arguments);
    }
});