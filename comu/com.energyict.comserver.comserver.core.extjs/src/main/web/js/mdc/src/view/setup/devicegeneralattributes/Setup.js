Ext.define('Mdc.view.setup.devicegeneralattributes.Setup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceGeneralAttributesSetup',
    itemId: 'deviceGeneralAttributesSetup',

    requires: [
        'Mdc.view.setup.device.DeviceMenu',
        'Uni.property.form.Property'
    ],

    content: [
        {
            xtype: 'panel',
            ui: 'large',
            title: Uni.I18n.translate('deviceconfigurationmenu.generalAttributes', 'MDC', 'General attributes'),
            layout: {
                type: 'hbox',
                align: 'stretch'
            },
            items: [
                {
                    xtype: 'property-form',
                    isEdit: false,
                    layout: 'column',

                    defaults: {
                        xtype: 'container',
                        layout: 'form',
                        resetButtonHidden: true,
                        labelWidth: 250,
                        columnWidth: 0.5
                    },
                    flex: 1
                },
                {
                    xtype: 'container',
                    itemId: 'editBtnContainer'
                }
            ]
        }
    ],

    initComponent: function () {
        var me = this;

            me.side = [
                {
                    xtype: 'panel',
                    title: Uni.I18n.translate('deviceregisterconfiguration.devices', 'MDC', 'Devices'),
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

        me.down('#editBtnContainer').add({
            xtype: 'button',
            itemId: 'deviceGeneralAttributesShowEdit',
            href: me.router.getRoute('devices/device/generalattributes/edit').buildUrl(),
            text: Uni.I18n.translate('general.edit', 'MDC', 'Edit')
        });


    }
});