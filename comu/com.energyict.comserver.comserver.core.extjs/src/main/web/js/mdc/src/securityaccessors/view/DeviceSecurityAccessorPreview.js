/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mdc.securityaccessors.view.DeviceSecurityAccessorPreview', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.device-security-accessor-preview',
    frame: true,

    requires: [
        'Mdc.securityaccessors.view.DeviceSecurityAccessorPreviewForm',
        //'Mdc.securityaccessors.view.SecurityAccessorsActionMenu'
    ],

    initComponent: function () {
        var me = this;
        //me.tools = [
        //    {
        //        xtype: 'uni-button-action',
        //        privileges: Mdc.privileges.DeviceType.admin,
        //        itemId: 'mdc-security-accessor-preview-button',
        //        menu: {
        //            xtype: 'security-accessors-action-menu'
        //        }
        //    }
        //];

        me.items = {
            xtype: 'device-security-accessor-preview-form'
        };
        me.callParent(arguments);
    }

});