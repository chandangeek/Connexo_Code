Ext.define('Mdc.view.setup.devicegeneralattributes.Edit', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceGeneralAttributesEdit',
    itemId: 'deviceGeneralAttributesEdit',

    requires: [
        'Mdc.view.setup.device.DeviceMenu',
        'Uni.property.form.Property'
    ],

    content: [
        {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('deviceconfiguration.generalAttributes.edit', 'MDC', 'Edit general attributes'),
            layout: {
                type: 'hbox',
                align: 'stretch'
            },
            items: [
                {
                    xtype: 'property-form',
                    width: '100%'
                }
            ],
            dockedItems: {
                xtype: 'container',
                dock: 'bottom',
                margin: '20 0 0 265',
                layout: {
                    type: 'hbox',
                    align: 'stretch'
                },

                items: [
                    {
                        xtype: 'button',
                        itemId: 'deviceGeneralAttributesSaveBtn',

                        ui: 'action',
                        text: Uni.I18n.translate('general.save', 'MDC', 'Save')
                    },
                    {
                        xtype: 'button',
                        text: Uni.I18n.translate('general.restoreToDefaultSettings', 'MDC', 'Restore to default settings'),
                        icon: '../sky/build/resources/images/form/restore.png',
                        itemId: 'deviceGeneralAttributesRestoreDefaultBtn'
                    },
                    {
                        xtype: 'button',
                        ui: 'link',
                        itemId: 'deviceGeneralAttributesCancelBtn',
                        text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel')
                    }
                ]
            }
        }
    ],

    initComponent: function () {
        var me = this;

        me.side = [
            {
                xtype: 'panel',
                title: Uni.I18n.translate('general.devices', 'MDC', 'Devices'),
                ui: 'medium',
                items: [
                    {
                        xtype: 'deviceMenu',
                        itemId: 'stepsMenu',
                        device: me.device,
                        toggleId: 'deviceGeneralAttributesLink'
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});