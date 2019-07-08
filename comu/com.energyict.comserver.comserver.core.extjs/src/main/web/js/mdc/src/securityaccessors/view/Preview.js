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
        'Mdc.securityaccessors.view.SecurityAccessorsActionMenu'
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
            },
            {
                xtype: 'displayfield',
                margins: '7 0 10 0',
                itemId: 'previewPropertiesHeader'
            },
            {
                xtype: 'panel',
                ui: 'medium',
                itemId: 'previewPropertiesPanel',
                items: [
                    {
                        xtype: 'property-form',
                        isEdit: false,
                        defaults: {
                            labelWidth: 200,
                            columnWidth: 0.5
                        }
                    }
                ]
            },
            {
                xtype: 'displayfield',
                itemId: 'previewNoProperties',
                hidden: true,
                fieldLabel: ' ',
                renderer: function () {
                    return '<span style="font-style:italic;color: grey;">' + Uni.I18n.translate('keyRenewal.properties.notAvailable', 'MDC', 'No attributes are available') + '</span>';
                }
            }
        ];
        me.callParent(arguments);
    }

});