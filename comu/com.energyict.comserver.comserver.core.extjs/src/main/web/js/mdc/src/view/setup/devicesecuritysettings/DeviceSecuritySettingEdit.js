Ext.define('Mdc.view.setup.devicesecuritysettings.DeviceSecuritySettingEdit', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceSecuritySettingEdit',
    itemId: 'deviceSecuritySettingEdit',
    edit: false,
    modal: true,

    required: [
        'Uni.property.form.Property'
    ],

    isEdit: function () {
        return this.edit;
    },

    setEdit: function (edit, returnLink) {
        if (edit) {
            this.edit = edit;
            this.down('#addEditButton').setText(Uni.I18n.translate('general.save', 'MDC', 'Save'));
            this.down('#addEditButton').action = 'editDeviceSecuritySetting';
        } else {
            this.edit = edit;
            this.down('#addEditButton').setText(Uni.I18n.translate('general.add', 'MDC', 'Add'));
            this.down('#addEditButton').action = 'addDeviceSecuritySetting';
        }
        this.down('#cancelLink').href = returnLink;
    },

    initComponent: function () {
        var me = this;
        this.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'deviceMenu',
                        itemId: 'stepsMenu',
                        device: me.device,
                        toggleId: 'securitySettingLink'
                    }
                ]
            }
        ];
        this.content = [
            {
                xtype: 'container',
                layout: {
                    type: 'vbox',
                    align: 'stretch'
                },

                items: [
                    {
                        xtype: 'panel',
                        layout: {
                            type: 'hbox',
                            align: 'stretch'
                        },
                        defaults: {
                            xtype: 'container'
                        },
                        ui: 'large',
                        itemId: 'deviceSecuritySettingEditAddTitle',
                        items: [
                            {
                                flex: 1
                            },
                            {
                                layout: {
                                    type: 'vbox',
                                    align: 'center',
                                    pack: 'center'
                                },

                                items: [
                                    {
                                        xtype: 'container',
                                        flex: 1
                                    },
                                    {
                                        xtype: 'container',
                                        flex: 1
                                    }
                                ]
                            }
                        ]
                    },
                    {
                        xtype: 'form',
                        border: false,
                        itemId: 'deviceSecuritySettingEditForm',
                        layout: {
                            type: 'vbox',
                            align: 'stretch'
                        },
                        defaults: {
                            labelWidth: 250
                        },
                        items: [
                            {
                                xtype: 'displayfield',
                                fieldLabel: Uni.I18n.translate('general.name', 'MDC', 'Name'),
                                name: 'name'
                            },
                            {
                                xtype: 'displayfield',
                                fieldLabel: Uni.I18n.translate('general.status', 'MDC', 'Status'),
                                name: 'status',
                                renderer: function (value) {
                                    return Ext.String.htmlEncode(value.name);
                                }
                            },
                            {
                                xtype: 'displayfield',
                                fieldLabel: Uni.I18n.translate('deviceSecuritySetting.authenticationLevel', 'MDC', 'Authentication level'),
                                name: 'authenticationLevel',
                                renderer: function (value) {
                                    return Ext.String.htmlEncode(value.name);
                                }
                            },
                            {
                                xtype: 'displayfield',
                                fieldLabel: Uni.I18n.translate('deviceSecuritySetting.encryptionLevel', 'MDC', 'Encryption level'),
                                name: 'encryptionLevel',
                                renderer: function (value) {
                                    return Ext.String.htmlEncode(value.name);
                                }
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
                        width: '100%'
                    },
                    {
                        xtype: 'form',
                        border: false,
                        itemId: 'deviceSecuritySettingEditShowValuesForm',
                        layout: {
                            type: 'hbox',
                            align: 'stretch'
                        },
                        width: '100%',
                        defaults: {
                            labelWidth: 250
                        },
                        items: [
                            {
                                xtype: 'fieldcontainer',
                                fieldLabel: '&nbsp',
                                itemId: 'device-security-setting-show-value',
                                visible: false,
                                layout: 'hbox',
                                items: [
                                    {
                                        xtype: 'checkbox',
                                        name: 'showValues',
                                        checked: false,
                                        margin: '0 16 0 0',
                                        itemId: 'device-security-setting-show-value-checkbox'
                                    },
                                    {
                                        xtype: 'label',
                                        text: Uni.I18n.translate('general.showEncryptedValue', 'MDC', 'Show values')
                                    }
                                ]
                            }
                        ]
                    },
                    {
                        xtype: 'form',
                        border: false,
                        itemId: 'deviceSecuritySettingEditButtonsForm',
                        layout: {
                            type: 'vbox',
                            align: 'stretch'
                        },
                        width: '100%',
                        defaults: {
                            labelWidth: 250
                        },
                        items: [
                            {
                                xtype: 'fieldcontainer',
                                ui: 'actions',
                                fieldLabel: '&nbsp',
                                items: [
                                    {
                                        text: Uni.I18n.translate('general.save', 'MDC', 'Save'),
                                        xtype: 'button',
                                        ui: 'action',
                                        action: 'createAction',
                                        itemId: 'addEditButton'
                                    },
                                    {
                                        xtype: 'button',
                                        text: Uni.I18n.translate('general.restoreToDefaultSettings', 'MDC', 'Restore to default settings'),
                                        icon: '../sky/build/resources/images/form/restore.png',
                                        itemId: 'restoreAllButton',
                                        action: 'restoreAll'
                                    },
                                    {
                                        text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel'),
                                        xtype: 'button',
                                        ui: 'link',
                                        itemId: 'cancelLink',
                                        href: '#/administration/devicetypes/'

                                    }
                                ]
                            }
                        ]
                    }
                ]
            }
        ]
        ;
        this.callParent(arguments);
        if (this.isEdit()) {
            this.down('#addEditButton').setText(Uni.I18n.translate('general.save', 'MDC', 'Save'));
            this.down('#addEditButton').action = 'editDeviceSecuritySetting';
        } else {
            this.down('#addEditButton').setText(Uni.I18n.translate('general.add', 'MDC', 'Add'));
            this.down('#addEditButton').action = 'createDeviceSecuritySetting';
        }
        this.down('#cancelLink').href = this.returnLink;

    }

});



