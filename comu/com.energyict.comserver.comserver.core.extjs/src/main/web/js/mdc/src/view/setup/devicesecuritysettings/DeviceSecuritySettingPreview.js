/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.devicesecuritysettings.DeviceSecuritySettingPreview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.deviceSecuritySettingPreview',
    itemId: 'deviceSecuritySettingPreview',
    requires: [
        'Mdc.model.DeviceSecuritySetting',
        'Uni.property.form.Property',
        'Uni.util.FormEmptyMessage',
        'Mdc.view.setup.devicesecuritysettings.DeviceSecuritySettingActionMenu'
    ],
    layout: {
        type: 'card',
        align: 'stretch'
    },

    title: Uni.I18n.translate('general.details', 'MDC', 'Details'),

    tools: [
        {
            xtype: 'uni-button-action',
            privileges: Mdc.privileges.DeviceSecurity.viewOrEditLevels,
            menu: {
                xtype: 'device-security-setting-action-menu'
            }
        }
    ],

    items: [
        {
            xtype: 'panel',
            border: false,
            tbar: [
                {
                    xtype: 'component',
                    html: '<h4>' + Uni.I18n.translate('devicesecuritysetting.noSecuritySettingSelected', 'MDC', 'No security setting selected') + '</h4>'
                }
            ],
            items: [
                {
                    xtype: 'component',
                    height: '100px',
                    html: '<h5>' + Uni.I18n.translate('devicesecuritysetting.selectSecuritySetting', 'MDC', 'Select a security setting to see its details') + '</h5>'
                }
            ]

        },
        {
            xtype: 'form',
            border: false,
            itemId: 'deviceSecuritySettingPreviewForm',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            items: [
                {
                    xtype: 'container',
                    layout: {
                        type: 'column'
                    },
                    items: [
                        {
                            columnWidth: 0.49,
                            items: [
                                {
                                    xtype: 'displayfield',
                                    fieldLabel: Uni.I18n.translate('general.name', 'MDC', 'Name'),
                                    labelWidth: 200,
                                    name: 'name'
                                },
                                {
                                    xtype: 'displayfield',
                                    fieldLabel: Uni.I18n.translate('general.status', 'MDC', 'Status'),
                                    labelWidth: 200,
                                    name: 'status',
                                    renderer: function (value) {
                                        return Ext.String.htmlEncode(value.name);
                                    }
                                },
                                {
                                    xtype: 'displayfield',
                                    fieldLabel: Uni.I18n.translate('deviceSecuritySetting.authenticationLevel', 'MDC', 'Authentication level'),
                                    labelWidth: 200,
                                    name: 'authenticationLevel',
                                    itemId: 'mdc-deviceSecuritySettingPreview-authenticationLevel',
                                    renderer: function (value) {
                                        return Ext.String.htmlEncode(value.name);
                                    }
                                },
                                {
                                    xtype: 'displayfield',
                                    fieldLabel: Uni.I18n.translate('deviceSecuritySetting.encryptionLevel', 'MDC', 'Encryption level'),
                                    labelWidth: 200,
                                    name: 'encryptionLevel',
                                    itemId: 'mdc-deviceSecuritySettingPreview-encryptionLevel',
                                    renderer: function (value) {
                                        return Ext.String.htmlEncode(value.name);
                                    }
                                }
                            ]
                        },
                        {
                            columnWidth: 0.49,
                            items: [
                                {
                                    xtype: 'displayfield',
                                    fieldLabel: Uni.I18n.translate('securitySetting.securitySuite', 'MDC', 'Security suite'),
                                    labelWidth: 200,
                                    name: 'securitySuite',
                                    itemId: 'mdc-deviceSecuritySettingPreview-securitySuite',
                                    renderer: function (value) {
                                        return Ext.String.htmlEncode(value.name);

                                    }
                                },
                                {
                                    xtype: 'displayfield',
                                    fieldLabel: Uni.I18n.translate('securitySetting.requestSecurityLevel', 'MDC', 'Request security level'),
                                    labelWidth: 200,
                                    name: 'requestSecurityLevel',
                                    itemId: 'mdc-deviceSecuritySettingPreview-requestSecurityLevel',
                                    renderer: function (value) {
                                        return Ext.String.htmlEncode(value.name);
                                    }
                                },
                                {
                                    xtype: 'displayfield',
                                    fieldLabel: Uni.I18n.translate('securitySetting.responseSecurityLevel', 'MDC', 'Response security level'),
                                    labelWidth: 200,
                                    name: 'responseSecurityLevel',
                                    itemId: 'mdc-deviceSecuritySettingPreview-responseSecurityLevel',
                                    renderer: function (value) {
                                        return Ext.String.htmlEncode(value.name);
                                    }
                                }
                            ]
                        }
                    ]
                },
                {
                    xtype: 'form',
                    border: false,
                    itemId: 'deviceSecuritySettingDetailsTitle',
                    hidden: true,
                    defaults: {
                        labelWidth: 250,
                        labelAlign: 'left'
                    },
                    items: [
                        {
                            xtype: 'displayfield',
                            fieldLabel: Uni.I18n.translate('general.attributes', 'MDC', 'Attributes'),
                            renderer: function () {
                                return ''; // No dash!
                            }
                        }
                    ]
                },
                {
                    xtype: 'property-form',
                    isEdit: false,
                    layout: 'column',

                    defaults: {
                        xtype: 'container',
                        layout: 'form',
                        resetButtonHidden: true,
                        labelWidth: 200,
                        columnWidth: 0.49
                    }
                }
            ]
        }
    ],

    initComponent: function () {
        this.callParent(arguments);
    },

    updateColumns: function (securitySuite) {
        this.down('#mdc-deviceSecuritySettingPreview-securitySuite').setVisible(securitySuite);
        this.down('#mdc-deviceSecuritySettingPreview-requestSecurityLevel').setVisible(securitySuite);
        this.down('#mdc-deviceSecuritySettingPreview-responseSecurityLevel').setVisible(securitySuite);
    }
});


