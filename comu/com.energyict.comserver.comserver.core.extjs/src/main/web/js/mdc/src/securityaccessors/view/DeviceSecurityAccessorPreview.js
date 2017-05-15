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
    },

    doLoadRecord: function(record) {
        var me = this,
            hasEditRights = Mdc.securityaccessors.view.PrivilegesHelper.hasPrivileges(record.get('editLevels'));

        me.setTitle(Ext.htmlEncode(record.get('name')));
        me.down('device-security-accessor-preview-form form').loadRecord(record);
        me.down('#mdc-device-security-accessor-preview-button').setVisible(
            !me.keyMode || (me.keyMode && hasEditRights)
        );
    }

});