/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Pkj.view.TrustStorePreview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.truststore-preview',
    requires: [
        'Pkj.view.TrustStorePreviewForm',
        'Pkj.view.TrustStoreActionMenu'
    ],

    initComponent: function () {
        var me = this;
        me.tools = [
            {
                xtype: 'uni-button-action',
                //  privileges: Apr.privileges.AppServer.admin,
                menu: {
                    xtype: 'truststore-action-menu'
                }
            }
        ];

        me.items = {
            xtype: 'truststore-preview-form'
        };
        me.callParent(arguments);
    }

});