/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Sct.view.Preview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.servicecalltypes-preview',
    requires: [
        'Sct.view.PreviewForm',
    ],

    initComponent: function () {
        var me = this;
        me.tools = [
            {
                xtype: 'uni-button-action',
              //  privileges: Apr.privileges.AppServer.admin,
                menu: {
                    xtype: 'sct-action-menu'
                }
            }
        ];

        me.items = {
            xtype: 'servicecalltypes-preview-form',
        };
        me.callParent(arguments);
    }

});