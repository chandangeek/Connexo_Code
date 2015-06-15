Ext.define('Mdc.view.setup.devicesecuritysettings.DeviceSecuritySettingPreview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.deviceSecuritySettingPreview',
    itemId: 'deviceSecuritySettingPreview',
    requires: [
        'Mdc.model.DeviceSecuritySetting',
        'Uni.property.form.Property',
        'Mdc.view.setup.devicesecuritysettings.DeviceSecuritySettingActionMenu'
    ],
    layout: {
        type: 'card',
        align: 'stretch'
    },

    title: 'Details',

    tools: [
        {
            xtype: 'button',
            text: Uni.I18n.translate('general.actions', 'MDC', Uni.I18n.translate('general.actions', 'MDC', 'Actions')),
            privileges:Mdc.privileges.DeviceSecurity.viewOrEditLevels,
            iconCls: 'x-uni-action-iconD',
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
//                        align: 'stretch'
                    },
                    items: [
                        {
                            columnWidth: 0.49,
                            items: [
                                {
                                    xtype: 'displayfield',
                                    fieldLabel: Uni.I18n.translate('deviceSecuritySetting.name','MDC','Name'),
                                    labelWidth: 200,
                                    name: 'name'
                                },
                                {
                                    xtype: 'displayfield',
                                    fieldLabel: Uni.I18n.translate('deviceSecuritySetting.status','MDC','Status'),
                                    labelWidth: 200,
                                    name: 'status',
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
                                    fieldLabel: Uni.I18n.translate('deviceSecuritySetting.authenticationLevel','MDC','Authentication level'),
                                    labelWidth: 200,
                                    name: 'authenticationLevel',
                                    renderer: function (value) {
                                        return Ext.String.htmlEncode(value.name);
                                    }
                                },
                                {
                                    xtype: 'displayfield',
                                    fieldLabel: Uni.I18n.translate('deviceSecuritySetting.encryptionLevel','MDC','Encryption level'),
                                    labelWidth: 200,
                                    name: 'encryptionLevel',
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
                    items: [
                        {
                            xtype: 'displayfield',
                            fieldLabel: '<h3>' + Uni.I18n.translate('deviceSecuritySetting.details', 'MDC', 'Attributes') + '</h3>',
                            text: ''
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
    }
});


