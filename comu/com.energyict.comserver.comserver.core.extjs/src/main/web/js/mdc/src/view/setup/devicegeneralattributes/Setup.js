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
                    xtype: 'property-form',
                    isEdit: false,

                    defaults: {
                        xtype: 'container',
                        layout: 'form',
                        resetButtonHidden: true,
                        labelWidth: 250
                    }
                }
            ]
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
            privileges: Mdc.privileges.Device.administrateDeviceCommunication,
            href: typeof me.router.getRoute('devices/device/generalattributes/edit') !== 'undefined'
                ? me.router.getRoute('devices/device/generalattributes/edit').buildUrl() : null,
            text: Uni.I18n.translate('general.edit', 'MDC', 'Edit'),
            dynamicPrivilege: Mdc.dynamicprivileges.DeviceState.generalAttributesActions
        });


    }
});