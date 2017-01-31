/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.view.appservers.ImportServicePreview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.import-service-preview',
    router: null,

    requires: [
        'Apr.view.appservers.ImportServicePreviewForm'
    ],

    initComponent: function () {
        var me = this;
        me.tools = [
            {
                xtype: 'uni-button-action',
                privileges: Apr.privileges.AppServer.admin,
                menu: {
                    xtype: 'apr-import-services-action-menu'
                }
            }
        ];
        me.items = {
            xtype: 'import-service-preview-form',
            router: me.router
        };
        me.callParent(arguments);
    }
});