Ext.define('Fwc.view.firmware.FirmwareOptions', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.firmware-options',
    itemId: 'firmware-options',
    requires: [
        'Mdc.view.setup.devicetype.SideMenu',
        'Fwc.model.FirmwareManagementOptions',
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
                title: Uni.I18n.translate('deviceType.firmwaremanagementoptions.title', 'FWC', 'Firmware management options'),
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
                                itemId: 'is-allowed',
                                fieldLabel: Uni.I18n.translate('deviceType.firmwaremanagementoptions.allowed', 'FWC', 'Firmware management allowed'),
                                renderer: function (value) {
                                    return value ? Uni.I18n.translate('general.yes', 'FWC', 'Yes') : Uni.I18n.translate('general.no', 'FWC', 'No');
                                }
                            },
                            {
                                xtype: 'fieldcontainer',
                                itemId: 'field-fw-upgrade-opts',
                                fieldLabel: Uni.I18n.translate('deviceType.firmwaremanagementoptions.options', 'FWC', 'Firmware management options'),
                                items: [
                                    {
                                        xtype: 'emptygridcontainer',
                                        grid: {
                                            xtype: 'options-grid',
                                            itemId: 'options-grid',
                                            scroll: false,
                                            name: 'allowedOptions',
                                            store: Ext.create('Ext.data.Store', {
                                                fields: ['localizedValue']
                                            }),
                                            emptyText: Uni.I18n.translate('deviceType.firmwaremanagementoptions.off', 'FWC', 'Firmware management is off')
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
                                itemId: 'button-edit',
                                name: 'Edit',
                                text: Uni.I18n.translate('deviceType.firmwaremanagementoptions.edit', 'FWC', 'Edit'),
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


