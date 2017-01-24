Ext.define('Mdc.view.setup.devicegeneralattributes.Setup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceGeneralAttributesSetup',
    itemId: 'deviceGeneralAttributesSetup',

    requires: [
        'Mdc.view.setup.device.DeviceMenu',
        'Uni.property.form.Property',
        'Uni.view.menu.ActionsMenu'
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

        me.content = [
            {
                xtype: 'panel',
                ui: 'large',
                title: Uni.I18n.translate('general.generalAttributes', 'MDC', 'General attributes'),
                layout: {
                    type: 'vbox',
                    align: 'stretch'
                },
                tools: [
                    {
                        xtype: 'uni-button-action',
                        itemId: 'mdc-general-attributes-actions-button',
                        dynamicPrivilege: Mdc.dynamicprivileges.DeviceState.generalAttributesActions,
                        menu: {
                            xtype: 'uni-actions-menu',
                            itemId: 'editBtnContainer',
                            items: [
                                {
                                    itemId: 'deviceGeneralAttributesShowEdit',
                                    privileges: Mdc.privileges.Device.administrateDeviceCommunication,
                                    href: typeof me.router.getRoute('devices/device/generalattributes/edit') !== 'undefined'
                                        ? me.router.getRoute('devices/device/generalattributes/edit').buildUrl() : null,
                                    text: Uni.I18n.translate('general.edit', 'MDC', 'Edit'),
                                    dynamicPrivilege: Mdc.dynamicprivileges.DeviceState.generalAttributesActions
                                }
                            ]
                        }
                    }
                ],
                items: [
                    {
                        xtype: 'property-form',
                        isEdit: false,
                        defaults: {
                            resetButtonHidden: true,
                            labelWidth: 250
                        }
                    }
                ]
            }
        ];
        me.callParent(arguments);
    }
});