Ext.define('Mdc.view.setup.deviceconfiguration.DeviceConfigurationEdit', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceConfigurationEdit',
    itemId: 'deviceConfigurationEdit',
    edit: false,
    isEdit: function () {
        return this.edit
    },

    initComponent: function () {
        this.content = [
            {
                xtype: 'container',
                cls: 'content-container',
                layout: {
                    type: 'vbox',
                    align: 'stretch'
                },

                items: [
                    {
                        xtype: 'component',
                        html: '',
                        itemId: 'deviceConfigurationEditCreateTitle'
                    },
                    {
                        xtype: 'container',
                        layout: {
                            type: 'column'
                        },
                        items: [
                            {
                                xtype: 'container',
                                columnWidth: 0.5,
                                items: [
                                    {
                                        xtype: 'form',
                                        border: false,
                                        itemId: 'deviceConfigurationEditForm',
                                        layout: {
                                            type: 'vbox',
                                            align: 'stretch'
                                        },
                                        defaults: {
                                            labelWidth: 250
                                        },
                                        items: [
                                            {
                                                xtype: 'textfield',
                                                name: 'name',
                                                msgTarget: 'under',
                                                required: true,
                                                fieldLabel: Uni.I18n.translate('deviceconfiguration.name', 'MDC', 'Name'),
                                                itemId: 'editDeviceConfigurationNameField',
                                                maxLength: 80,
                                                enforceMaxLength: true
                                            },
                                            {
                                                xtype: 'textareafield',
                                                name: 'description',
                                                height: 100,
                                                msgTarget: 'under',
                                                fieldLabel: Uni.I18n.translate('deviceconfiguration.description', 'MDC', 'Description'),
                                                itemId: 'editDeviceConfigurationDescriptionField'
                                            },
                                            {
                                                xtype: 'checkbox',
                                                inputValue: true,
                                                uncheckedValue: 'false',
                                                name: 'canBeGateway',
                                                fieldLabel: Uni.I18n.translate('deviceconfiguration.isGateway', 'MDC', 'Can act as gateway'),
                                                itemId: 'gatewayCheckbox',
                                                msgTarget: 'under'
                                            },
                                            {
                                                xtype: 'fieldcontainer',
                                                columnWidth: 0.5,
                                                fieldLabel: ' ',
                                                layout: {
                                                    type: 'hbox'
                                                },
                                                hidden: true,
                                                itemId: 'gatewayMessage',
                                                items: [
                                                    {
                                                        xtype: 'component',
                                                        cls: 'x-form-display-field',
                                                        html: '<span style="color: grey"><i>' + Uni.I18n.translate('deviceconfiguration.gatewayMessage', 'MDC', 'The device can not act as a gateway') + '</i>'
                                                    }
                                                ]
                                            },
                                            {
                                                xtype: 'checkbox',
                                                inputValue: true,
                                                uncheckedValue: 'false',
                                                name: 'isDirectlyAddressable',
                                                fieldLabel: Uni.I18n.translate('deviceconfiguration.isDirectlyAddressable', 'MDC', 'Directly addressable'),
                                                itemId: 'addressableCheckbox',
                                                msgTarget: 'under'
                                            },
                                            {
                                                xtype: 'fieldcontainer',
                                                columnWidth: 0.5,
                                                fieldLabel: ' ',
                                                layout: {
                                                    type: 'hbox'
                                                },
                                                hidden: true,
                                                itemId: 'addressableMessage',
                                                items: [
                                                    {
                                                        xtype: 'component',
                                                        cls: 'x-form-display-field',
                                                        html: '<span style="color: grey"><i>' + Uni.I18n.translate('deviceconfiguration.directlyAddressableMessage', 'MDC', 'The device cannot be directly addressed') + '</i>'
                                                    }
                                                ]
                                            },
                                            {
                                                xtype: 'fieldcontainer',
                                                ui: 'actions',
                                                fieldLabel: '&nbsp',
                                                layout: {
                                                    type: 'hbox'
                                                },
                                                items: [
                                                    {
                                                        text: Uni.I18n.translate('general.add', 'MDC', 'Add'),
                                                        xtype: 'button',
                                                        ui: 'action',
                                                        action: 'createAction',
                                                        itemId: 'createEditButton'
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
                    }


                ]
            }
        ];
        this.callParent(arguments);
        if (this.isEdit()) {
            this.down('#createEditButton').setText(Uni.I18n.translate('general.save', 'MDC', 'Save'));
            this.down('#createEditButton').action = 'editDeviceConfiguration';
        } else {
            this.down('#createEditButton').setText(Uni.I18n.translate('general.add', 'MDC', 'Add'));
            this.down('#createEditButton').action = 'createDeviceConfiguration';
        }
        this.down('#cancelLink').href = this.returnLink;
    }

});