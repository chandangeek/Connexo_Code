Ext.define('Mdc.view.setup.devicesecuritysettings.DeviceSecuritySettingEdit', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceSecuritySettingEdit',
    itemId: 'deviceSecuritySettingEdit',
    edit: false,

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
        this.content = [
            {
                xtype: 'container',
                layout: {
                    type: 'vbox'
                    //align: 'stretch'
                },

                items: [
                    {
                        xtype: 'component',
                        html: '',
                        itemId: 'deviceSecuritySettingEditAddTitle'
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
                                fieldLabel: Uni.I18n.translate('securitySetting.name','MDC','Name'),
                                name: 'name'
                            },
                            {
                                xtype: 'displayfield',
                                fieldLabel: Uni.I18n.translate('securitySetting.status','MDC','Status'),
                                name: 'status',
                                renderer: function (value) {
                                    return value.name;
                                }
                            },
                            {
                                xtype: 'displayfield',
                                fieldLabel: Uni.I18n.translate('securitySetting.authenticationLevel','MDC','Authentication level'),
                                name: 'authenticationLevel',
                                renderer: function (value) {
                                    return value.name;
                                }
                            },
                            {
                                xtype: 'displayfield',
                                fieldLabel: Uni.I18n.translate('securitySetting.encryptionLevel','MDC','Encryption level'),
                                name: 'encryptionLevel',
                                renderer: function (value) {
                                    return value.name;
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
                                fieldLabel: '<h3>' + Uni.I18n.translate('securitySetting.details', 'MDC', 'Attributes') + '</h3>',
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
                                        text: Uni.I18n.translate('general.restoreAll', 'MDC', 'Restore to defaults'),
                                        xtype: 'button',
                                        itemId: 'restoreAllButton',
                                        action: 'restoreAll',
                                        margin: '0 0 0 10',
                                        disabled: true
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



