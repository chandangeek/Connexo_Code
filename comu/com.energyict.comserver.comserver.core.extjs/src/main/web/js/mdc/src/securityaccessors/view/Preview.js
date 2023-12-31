/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mdc.securityaccessors.view.Preview', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.security-accessors-preview',
    frame: true,
    deviceTypeId: true,

    requires: [
        'Mdc.securityaccessors.view.PreviewForm',
        'Mdc.securityaccessors.view.SecurityAccessorsActionMenu',
        'Mdc.securityaccessors.view.KeyRenewalOverviewProperties',
    ],

    initComponent: function () {
        var me = this;
        me.tools = [
            {
                xtype: 'uni-button-action',
                privileges: Mdc.privileges.DeviceType.admin,
                disabled: !Mdc.privileges.SecurityAccessor.canAdmin(),
                itemId: 'mdc-security-accessor-preview-button',
                menu: {
                    xtype: 'security-accessors-action-menu',
                    deviceTypeId: me.deviceTypeId
                }
            }
        ];

        me.items = [
            {
                xtype: 'devicetype-security-accessors-preview-form',
                itemId: 'mdc-devicetype-security-accessors-preview-form'
            }
        ];
        me.callParent(arguments);
    }

});