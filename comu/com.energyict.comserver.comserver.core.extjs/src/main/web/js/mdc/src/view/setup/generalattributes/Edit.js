Ext.define('Mdc.view.setup.generalattributes.Edit', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.generalAttributesEdit',
    itemId: 'generalAttributesEdit',

    requires: [
        'Mdc.view.setup.deviceconfiguration.DeviceConfigurationMenu',
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
                        itemId: 'generalAttributesSaveBtn',

                        ui: 'action',
                        text: Uni.I18n.translate('general.save', 'MDC', 'Save')
                    },
                    {
                        xtype: 'button',
                        text: Uni.I18n.translate('general.restoreToDefaultSettings', 'MDC', 'Restore to default settings'),
                        icon: '../sky/build/resources/images/form/restore.png',
                        itemId: 'generalAttributesRestoreDefaultBtn'
                    },
                    {
                        xtype: 'button',
                        ui: 'link',
                        itemId: 'generalAttributesCancelBtn',
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
                ui: 'medium',
                items: [
                    {
                        xtype: 'device-configuration-menu',
                        toggle: 10,
                        deviceTypeId: me.router.arguments.deviceTypeId,
                        deviceConfigurationId: me.router.arguments.deviceConfigurationId
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});