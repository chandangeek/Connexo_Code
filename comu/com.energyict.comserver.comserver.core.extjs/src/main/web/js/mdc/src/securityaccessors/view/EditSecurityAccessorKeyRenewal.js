Ext.define('Mdc.securityaccessors.view.EditSecurityAccessorKeyRenewal', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.edit-key-renewal-config',
    requires: [
        'Mdc.securityaccessors.widget.CommandForm',
        'Uni.property.form.Property',
        'Uni.util.FormErrorMessage'
    ],
    device: null,
    initComponent: function () {
        var me = this;
        me.content = [
            {
                xtype: 'form',
                ui: 'large',
                itemId: 'key-renewal-command-add-panel',
                title: Uni.I18n.translate('deviceCommand.add.title', 'MDC', 'Add command'),
                layout: {
                    type: 'vbox',
                    align: 'stretch'
                },
                items: [
                    {
                        xtype: 'uni-form-error-message',
                        name: 'form-errors',
                        itemId: 'form-errors',
                        margin: '0 0 10 0',
                        maxWidth: 600,
                        hidden: true
                    },
                    {
                        xtype: 'key-renewal-edit-form',
                        margin: '32 0 0 0',
                        itemId: 'key-renewal-edit-form'
                    },
                    {
                        itemId: 'key-renewal-command-add-property-header',
                        margin: '16 0 0 0'
                    },
                    {
                        itemId: 'key-renewal-command-add-property-form',
                        xtype: 'property-form',
                        margin: '16 0 0 0',
                        defaults: {
                            labelWidth: 250,
                            resetButtonHidden: false,
                            width: 336 // To be aligned with the above
                        }
                    },
                    {
                        xtype: 'fieldcontainer',
                        ui: 'actions',
                        fieldLabel: '&nbsp',
                        labelWidth: 250,
                        layout: {
                            type: 'hbox'
                        },
                        items: [
                            {
                                xtype: 'button',
                                itemId: 'mdc-keyRenewalConfig-add-button',
                                text: Uni.I18n.translate('general.add', 'MDC', 'Add'),
                                ui: 'action',
                                action: 'add',
                                deviceId: me.device.get('name')
                            },
                            {
                                xtype: 'button',
                                itemId: 'mdc-keyRenewalConfig-cancel-button',
                                text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel'),
                                ui: 'link',
                                action: 'cancel',
                                deviceId: me.device.get('name')
                            }
                        ]
                    }
                ]
            }
        ];
        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'deviceMenu',
                        itemId: 'stepsMenu',
                        toggleId: 'deviceCommands',
                        device: me.device
                    }
                ]
            }
        ];
        me.callParent(arguments);
    }
});