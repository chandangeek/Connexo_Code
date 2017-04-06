/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mdc.securityaccessors.view.DeviceSecurityAccessorPreview', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.device-security-accessor-preview',
    frame: true,
    keyMode: undefined,

    requires: [
        'Mdc.securityaccessors.view.DeviceSecurityAccessorPreviewForm',
        'Mdc.securityaccessors.view.DeviceSecurityAccessorsActionMenu'
    ],

    initComponent: function () {
        var me = this;
        me.tools = [
            {
                xtype: 'uni-button-action',
                //privileges: Mdc.privileges.DeviceType.admin,
                itemId: 'mdc-device-security-accessor-preview-button',
                menu: {
                    xtype: 'device-security-accessors-action-menu',
                    keyMode: me.keyMode
                }
            }
        ];

        me.items = {
            xtype: 'device-security-accessor-preview-form',
            keyMode: me.keyMode
        };
        me.callParent(arguments);
    }

});