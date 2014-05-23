Ext.define('Mdc.view.setup.securitysettings.SecuritySettingPreview', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.securitySettingPreview',
//    height: 310,
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
                layout: 'form',
                columnWidth: 0.5
            },
            items: [
                {
                    items: [
                        {
                            xtype: 'displayfield',
                            fieldLabel: 'Name',
                            name: 'name'
                        }
                    ]
                },
                {
                    items: [
                        {
                            xtype: 'displayfield',
                            fieldLabel: 'Authentication level',
                            name: 'authenticationLevel',
                            renderer: function (value) {
                                return value.name;
                            }
                        },
                        {
                            xtype: 'displayfield',
                            fieldLabel: 'Encryption level',
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


