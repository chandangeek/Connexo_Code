Ext.define('Mdc.view.setup.securitysettings.SecuritySettingForm', {
    extend: 'Uni.view.container.ContentContainer',
    requires: [
        'Uni.view.navigation.SubMenu',
        'Ext.form.field.TextArea',
        'Ext.button.Button'
    ],

    alias: 'widget.securitySettingForm',
    securityHeader: null,
    deviceTypeId: null,
    deviceConfigurationId: null,
    actionButtonName: null,

    content: [
        {
            items: [
                {
                    xtype: 'container',
                    itemId: 'SecuritySettingHeader'
                },
                {
                    xtype: 'form',
                    width: '50%',
                    defaults: {
                        labelWidth: 150,
                        labelAlign: 'right',
                        margin: '0 0 20 0',
                        validateOnChange: false,
                        validateOnBlur: false,
                        anchor: '100%'
                    },
                    items: [
                        {
                            name: 'errors',
                            layout: 'hbox',
                            margin: '0 0 20 100',
                            hidden: true,
                            defaults: {
                                xtype: 'container'
                            }
                        },
                        {
                            xtype: 'textfield',
                            name: 'name',
                            labelSeparator: ' *',
                            regex: /[a-zA-Z0-9]+/,
                            allowBlank: false,
                            fieldLabel: 'Name',
                            msgTarget: 'under'
                        },
                        {
                            xtype: 'combobox',
                            labelSeparator: ' ',
                            allowBlank: false,
                            fieldLabel: 'Authentication level',
                            emptyText: 'Select authentication level',
                            name: 'authenticationLevelId',
                            displayField: 'name',
                            valueField: 'id',
                            queryMode: 'local'
                        },
                        {
                            xtype: 'combobox',
                            labelSeparator: ' ',
                            allowBlank: false,
                            fieldLabel: 'Encryption level',
                            emptyText: 'Select encryption level',
                            name: 'encryptionLevelId',
                            displayField: 'name',
                            valueField: 'id',
                            queryMode: 'local'
                        }
                    ],
                    dockedItems: [
                        {
                            xtype: 'toolbar',
                            dock: 'bottom',
                            border: false,
                            margin: '0 0 0 100',
                            items: [
                                {
                                    xtype: 'container',
                                    itemId: 'SecurityAction'
                                },
                                {
                                    xtype: 'container',
                                    itemId: 'SecuritySettingCancel'
                                }
                            ]
                        }
                    ]
                }
            ]
        }
    ],

    initComponent: function () {
        this.callParent(this);
        this.down('#SecuritySettingCancel').add(
            {
                xtype: 'button',
                text: 'Cancel',
                name: 'cancel',
                margin: 10,
                hrefTarget: '',
                href: '#/administration/devicetypes/' + this.deviceTypeId + '/deviceconfigurations/' + this.deviceConfigurationId + '/securitysettings',
                cls: 'isu-btn-link'
            }
        );
        this.down('#SecuritySettingHeader').add(
            {
                xtype: 'container',
                html: '<h2>' + this.securityHeader + '</h2>'
            }
        );
        this.down('#SecurityAction').add(
            {
                xtype: 'button',
                name: 'securityaction',
                text: this.actionButtonName
            }
        );
    }
});

