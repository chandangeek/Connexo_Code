Ext.define('Mdc.view.setup.securitysettings.SecuritySettingPreview', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.securitySettingPreview',
    frame: true,
    tools: [
        {
            xtype: 'button',
            text: 'Actions',
            iconCls: 'x-uni-action-iconA',
            menu: {
                xtype: 'menu',
                plain: true,
                border: false,
                shadow: false,
                items: [
                    {
                        text: 'Edit',
                        action: 'editsecuritysetting'
                    },
                    {
                        text: 'Remove',
                        action: 'deletesecuritysetting'
                    }
                ]
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
                            fieldLabel: 'Name',
                            name: 'name'
                        }
                    ]
                },
                {
                    columnWidth: 0.6,
                    items: [
                        {
                            xtype: 'displayfield',
                            fieldLabel: 'Authentication level',
                            labelWidth: 200,
                            name: 'authenticationLevel',
                            renderer: function (value) {
                                return value.name;
                            }
                        },
                        {
                            xtype: 'displayfield',
                            fieldLabel: 'Encryption level',
                            labelWidth: 200,
                            name: 'encryptionLevel',
                            renderer: function (value) {
                                return value.name;
                            }
                        }
                    ]
                }
            ]
        }
    ]

});


