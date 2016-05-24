Ext.define('Fwc.view.firmware.FirmwareOptions', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.firmware-options',
    itemId: 'firmware-options',
    requires: [
        'Mdc.view.setup.devicetype.SideMenu',
        'Fwc.model.FirmwareManagementOptions',
        'Fwc.view.firmware.OptionsGrid',
        'Fwc.view.firmware.FirmwareOptionsXTemplate',
        'Fwc.view.firmware.FirmwareOptionsEdit'
    ],
    deviceType: null,
    model: null,

    initComponent: function () {
        var me = this;

        me.content = [
            {
                ui: 'large',
                layout: 'hbox',
                tools: [
                    {
                        xtype: 'button',
                        itemId: 'fwc-specifications-actions-btn',
                        iconCls: 'x-uni-action-iconD',
                        text: Uni.I18n.translate('general.actions', 'FWC', 'Actions'),
                        menu: {
                            plain: true,
                            border: false,
                            shadow: false,
                            items: [
                                {
                                    itemId: 'mdc-edit-options-btn',
                                    text: Uni.I18n.translate('general.edit', 'FWC', 'Edit'),
                                    privileges: Mdc.privileges.DeviceType.admin,
                                    action: 'editFirmwareOptions'
                                }
                            ]
                        }
                    }
                ],
                items: [
                    {
                        xtype: 'form',
                        padding: '15 0 0 0',
                        itemId: 'form',
                        model: 'FirmwareManagementOptions',
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
                                xtype: 'displayfield',
                                name: 'supportedOptions',
                                itemId: 'supported-options',
                                fieldLabel: ' ',
                                style: 'margin-top: -15px',
                                fieldStyle: 'font-style: italic;',
                                hidden: true,
                                renderer: function (value, field) {
                                    var result = '',
                                        record = field.up('form').getRecord();
                                    if (record && !record.get('supportedOptions').length) {
                                        result = Uni.I18n.translate('deviceType.firmwaremanagementoptions.notAllowedByProtocol', 'FWC', 'Firmware management is not supported by the communication protocol of this device type');
                                        field.show();
                                    } else {
                                        field.hide();
                                    }
                                    return result;
                                }
                            },
                            {
                                xtype: 'displayfield',
                                name: 'allowedOptions',
                                itemId: 'allowed-options',
                                fieldStyle: 'margin-top : 3px;',
                                fieldLabel: Uni.I18n.translate('general.firmwareManagementOptions', 'FWC', 'Firmware management options'),
                                renderer: function (value) {
                                    var tpl = Ext.create('FirmwareOptionsXTemplate');
                                    return tpl.apply(value);
                                }
                            }
                        ],
                        loadRecord: function (record) {
                            this.getForm().loadRecord(record);
                            if (record.get('supportedOptions').length === 0) {
                                me.down('button#button-edit').disable();
                            }
                        }
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});


