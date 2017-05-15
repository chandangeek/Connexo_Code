/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mdc.securityaccessors.view.Setup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.device-type-security-accessors-setup',

    requires: [
        'Mdc.view.setup.devicetype.SideMenu',
        'Mdc.securityaccessors.view.PreviewContainer',
        'Uni.util.FormEmptyMessage'
    ],

    deviceTypeId: null,

    initComponent: function () {
        var me = this;
        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'deviceTypeSideMenu',
                        itemId: 'deviceTypeSideMenu',
                        deviceTypeId: me.deviceTypeId,
                        toggle: 0
                    }
                ]
            }
        ];

        me.content = {
            ui: 'large',
            title: Uni.I18n.translate('general.securityAccessors', 'MDC', 'Security accessors'),
            items: [
                {
                    xtype: 'security-accessors-preview-container',
                    itemId: 'mdc-security-accessors-preview-container',
                    deviceTypeId: me.deviceTypeId
                }
            ]
        };

        me.callParent(arguments);
    }
});