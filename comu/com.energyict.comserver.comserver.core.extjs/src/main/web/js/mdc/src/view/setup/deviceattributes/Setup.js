Ext.define('Mdc.view.setup.deviceattributes.Setup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceAttributesSetup',
    itemId: 'device-attributes-setup',

    requires: [
        'Mdc.view.setup.device.DeviceMenu',
        'Mdc.view.setup.device.DeviceAttributesForm'
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

        me.content = [
            {
                xtype: 'panel',
                ui: 'large',
                title: Uni.I18n.translate('devicemenu.deviceAttributes', 'MDC', 'Device attributes'),
                layout: {
                    type: 'vbox',
                    align: 'stretch'
                },
                items: [
                    {
                        xtype: 'container',
                        layout: {
                            type: 'hbox',
                            align: 'stretch'
                        },
                        items: [
                            {
                                xtype: 'container',
                                flex: 1
                            },
                            {
                                xtype: 'container',
                                itemId: 'editBtnContainer'
                            }
                        ]
                    },
                    {
                        xtype: 'deviceAttributesForm',
                        itemId: 'device-attributes-view-form',
                        router: me.router,
                        fullInfo: true
                    }
                ]
            }
        ];

        me.callParent(arguments);

        me.down('#editBtnContainer').add({
            xtype: 'button',
            itemId: 'deviceDeviceAttributesShowEdit',
            privileges: Mdc.privileges.Device.editDeviceAttributes,
            href: typeof me.router.getRoute('devices/device/attributes/edit') !== 'undefined'
                ? me.router.getRoute('devices/device/attributes/edit').buildUrl() : null,
            text: Uni.I18n.translate('general.edit', 'MDC', 'Edit')
        });
    }
});