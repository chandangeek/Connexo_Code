Ext.define('Mdc.view.setup.securitysettings.SecuritySettingPreview', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.securitySettingPreview',
    frame: true,


    tools: [
        {
            xtype: 'button',
            text: Uni.I18n.translate('general.actions', 'MDC', Uni.I18n.translate('general.actions', 'MDC', 'Actions')),
            privileges: Mdc.privileges.DeviceType.admin,
            iconCls: 'x-uni-action-iconD',
            menu: {
                xtype: 'security-settings-action-menu'
            }
        }
    ],

    items: [
        {
            xtype: 'form',
            name: 'securitySettingDetails',
            layout: 'column',
            defaults: {
                xtype: 'container',
                layout: 'form'
            },
            items: [
                {
                    columnWidth: 0.4,
                    items: [
                        {
                            xtype: 'displayfield',
                            fieldLabel: Uni.I18n.translate('securitySetting.name','MDC','Name'),
                            name: 'name'
                        }
                    ]
                },
                {
                    columnWidth: 0.6,
                    items: [
                        {
                            xtype: 'displayfield',
                            fieldLabel: Uni.I18n.translate('securitySetting.authenticationLevel','MDC','Authentication level'),
                            labelWidth: 200,
                            name: 'authenticationLevel',
                            renderer: function (value) {
                                return Ext.String.htmlEncode(value.name);
                            }
                        },
                        {
                            xtype: 'displayfield',
                            fieldLabel: Uni.I18n.translate('securitySetting.encryptionLevel','MDC','Encryption level'),
                            labelWidth: 200,
                            name: 'encryptionLevel',
                            renderer: function (value) {
                                return Ext.String.htmlEncode(value.name);
                            }
                        }
                    ]
                }
            ]
        }
    ]

});


