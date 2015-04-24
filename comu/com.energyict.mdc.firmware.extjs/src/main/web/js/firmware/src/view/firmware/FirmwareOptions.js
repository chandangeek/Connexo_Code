Ext.define('Fwc.view.firmware.FirmwareOptions', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.firmware-options',
    itemId: 'firmware-options',
    requires: [
        'Mdc.view.setup.devicetype.SideMenu',
        'Fwc.model.FirmwareUpgradeOptions',
        'Fwc.view.firmware.OptionsGrid',
        'Fwc.view.firmware.FirmwareOptionsEdit'
    ],
    deviceType: null,
    model: null,

    initComponent: function () {
        var me = this;

        me.side = [
            {
                xtype: 'panel',
                ui: 'medium',
                items: [
                    {
                        xtype: 'deviceTypeSideMenu',
                        itemId: 'stepsMenu',
                        router: me.router,
                        deviceTypeId: me.deviceType.getId()
                    }
                ]
            }
        ];

        me.content = [
            {
                xtype: 'form',
                ui: 'large',
                layout: 'hbox',
                title: Uni.I18n.translate('deviceType.firmwareupgradeoptions.title', 'FWC', 'Firmware upgrade options'),
                items: [
                    {
                        xtype: 'container',
                        margin: '60 0 0 0',
                        flex: 1,
                        defaults: {
                            labelWidth: 250
                        },
                        items: [
                            {
                                xtype: 'displayfield',
                                name: 'isAllowed',
                                fieldLabel: Uni.I18n.translate('deviceType.firmwareupgradeoptions.allowed', 'FWC', 'Firmware upgrade allowed'),
                                renderer: function (value) {
                                    return value ? Uni.I18n.translate('general.yes', 'FWC', 'Yes') : Uni.I18n.translate('general.no', 'FWC', 'No');
                                }
                            },
                            {
                                xtype: 'fieldcontainer',
                                fieldLabel: Uni.I18n.translate('deviceType.firmwareupgradeoptions.options', 'FWC', 'Firmware upgrade options'),
                                items: [
                                    {
                                        xtype: 'emptygridcontainer',
                                        grid: {
                                            xtype: 'options-grid',
                                            scroll: false,
                                            name: 'allowedOptions',
                                            store: Ext.create('Ext.data.Store', {
                                                fields: ['localizedValue']
                                            }),
                                            emptyText: Uni.I18n.translate('deviceType.firmwareupgradeoptions.notsupported', 'FWC', 'No options supported by current device type')
                                        }
                                    }

                                ]
                            }
                        ]
                    },
                    {
                        xtype: 'container',
                        items: [
                            {
                                xtype: 'button',
                                name: 'Edit',
                                text: Uni.I18n.translate('deviceType.firmwareupgradeoptions.edit', 'FWC', 'Edit'),
                                action: 'editFirmwareOptions'
                            }
                        ]
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});


