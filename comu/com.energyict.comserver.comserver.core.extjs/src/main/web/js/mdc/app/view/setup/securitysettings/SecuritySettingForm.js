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
            xtype: 'panel',
            ui: 'large',
            itemId: 'SecuritySettingPanel',
            items: [
                {
                    xtype: 'form',
                    width: '50%',
                    defaults: {
                        labelWidth: 250,
                        validateOnChange: false,
                        validateOnBlur: false,
                        anchor: '100%'
                    },
                    items: [
                        {
                            name: 'errors',
                            ui: 'form-error-framed',
                            layout: 'hbox',
                            margin: '0 0 10 0',
                            hidden: true,
                            defaults: {
                                xtype: 'container'
                            }
                        },
                        {
                            xtype: 'textfield',
                            name: 'name',
                            required: true,
                            regex: /[a-zA-Z0-9]+/,
                            allowBlank: false,
                            fieldLabel: 'Name',
                            msgTarget: 'under'
                        },
                        {
                            xtype: 'combobox',
                            required: true,
                            editable: false,
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
                            required: true,
                            editable: false,
                            allowBlank: false,
                            fieldLabel: 'Encryption level',
                            emptyText: 'Select encryption level',
                            name: 'encryptionLevelId',
                            displayField: 'name',
                            valueField: 'id',
                            queryMode: 'local'
                        }
                    ],
                    buttons: [
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
    ],

    initComponent: function () {
        this.callParent(this);
        this.down('#SecuritySettingCancel').add(
            {
                xtype: 'button',
                text: 'Cancel',
                name: 'cancel',
                hrefTarget: '',
                href: '#/administration/devicetypes/' + this.deviceTypeId + '/deviceconfigurations/' + this.deviceConfigurationId + '/securitysettings',
                ui: 'link'
            }
        );
        this.down('#SecuritySettingPanel').setTitle(this.securityHeader);
        this.down('#SecurityAction').add(
            {
                xtype: 'button',
                ui: 'action',
                name: 'securityaction',
                text: this.actionButtonName
            }
        );
    }
});

