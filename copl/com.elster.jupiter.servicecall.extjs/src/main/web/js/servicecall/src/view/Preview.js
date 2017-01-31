/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Scs.view.Preview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.servicecalls-preview',
    router: null,
    requires: [
        'Scs.view.PreviewForm',
        'Scs.view.ActionMenu'
    ],

    initComponent: function () {
        var me = this;
        me.tools = [
            {
                xtype: 'uni-button-action',
                privileges: Scs.privileges.ServiceCall.admin,
                itemId: 'previewMenuButton',
                menu: {
                    xtype: 'scs-action-menu'
                }
            }
        ];

        me.items = {
            xtype: 'servicecalls-preview-form',
            itemId: 'servicecall-grid-preview-form',
            router: me.router
        };
        me.callParent(arguments);
    }

});