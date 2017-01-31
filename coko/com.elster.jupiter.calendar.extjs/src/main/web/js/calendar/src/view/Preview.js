/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cal.view.Preview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.tou-preview',
    requires: [
        'Cal.view.PreviewForm',
        'Cal.view.ActionMenu'
    ],

    initComponent: function () {
        var me = this;
        me.tools = [
            {
                xtype: 'uni-button-action',
                //privileges: Scs.privileges.ServiceCall.admin,
                itemId: 'touPreviewMenuButton',
                menu: {
                    xtype: 'tou-action-menu'
                }
            }
        ];

        me.items = {
            xtype: 'tou-preview-form',
            itemId: 'tou-grid-preview-form'
        };
        me.callParent(arguments);
    }

});