Ext.define('Mdc.view.setup.deviceattributes.Edit', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceAttributesEdit',
    itemId: 'device-attributes-edit',

    requires: [
        'Mdc.view.setup.deviceattributes.DeviceAttributesEditForm'
    ],

    content: [
        {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('deviceconfiguration.deviceAttributes.edit', 'MDC', 'Edit device attributes'),
            layout: {
                type: 'hbox',
                align: 'stretch'
            },
            items: [
                {
                    margin: '40 0 0 0',
                    xtype: 'deviceAttributesEditForm',
                    itemId: 'device-attributes-edit-form'
                }
            ],
            dockedItems: {
                xtype: 'container',
                dock: 'bottom',
                margin: '20 0 0 160',
                layout: {
                    type: 'hbox',
                    align: 'stretch'
                },

                items: [
                    {
                        xtype: 'button',
                        itemId: 'deviceAttributesSaveBtn',
                        ui: 'action',
                        text: Uni.I18n.translate('general.save', 'MDC', 'Save')
                    },
                    {
                        xtype: 'button',
                        ui: 'link',
                        itemId: 'deviceAttributesCancelBtn',
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
                        xtype: 'deviceMenu',
                        itemId: 'stepsMenu',
                        device: me.device
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});