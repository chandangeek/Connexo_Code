Ext.define('Fwc.view.firmware.FirmwareOptionsEdit', {
    extend: 'Uni.view.container.ContentContainer',
    requires: [
        'Fwc.model.FirmwareUpgradeOptions',
        'Fwc.form.OptionsHydrator'
    ],
    alias: 'widget.firmware-options-edit',
    itemId: 'firmware-options-edit',
    deviceType: null,

    initComponent: function () {
        this.content = [
            {
                xtype: 'form',
                hydrator: 'Fwc.form.OptionsHydrator',
                title: Uni.I18n.translate('deviceType.firmwareupgradeoptions.edit', 'FWC', 'Edit firmware management options'),
                ui: 'large',
                border: false,
                width: 850,
                itemId: 'firmwareOptionsEditForm',
                layout: {
                    type: 'vbox',
                    align: 'stretch'
                },
                defaults: {
                    labelWidth: 250
                },
                loadRecord: function (record) {
                    var options = this.down('#firmwareUpgradeOptions');
                    options.items.each(function (item) {
                        if (record.get('supportedOptions')
                            .map(function (item) {return item.id; })
                            .indexOf(item.inputValue) < 0) {options.remove(item); }
                    });
                    this.getForm().loadRecord(record);
                },
                items: [
                    {

                        xtype: 'radiogroup',
                        fieldLabel: Uni.I18n.translate('deviceType.firmwareupgradeoptions.allowed', 'FWC', 'Firmware upgrade allowed'),
                        itemId: 'allowedCombo',
                        required: true,
                        columns: 1,
                        vertical: true,
                        listeners: {
                            change: function (radiogroup, newValue) {
                                var form = radiogroup.up('form'),
                                    checkboxgroup = form.down('#firmwareUpgradeOptions');

                                if (!newValue.isAllowed) {
                                    checkboxgroup.disable();
                                    checkboxgroup.setValue([]);
                                } else {
                                    checkboxgroup.enable();
                                }
                            }
                        },
                        defaults: {
                            name: 'isAllowed'
                        },
                        items: [
                            {
                                itemId: 'rbtn-is-allowed-yes',
                                boxLabel: '<b>' + Uni.I18n.translate('general.yes', 'MDC', 'Yes') + '</b>',
                                inputValue: 1
                            },
                            {
                                itemId: 'rbtn-is-allowed-no',
                                boxLabel: '<b>' + Uni.I18n.translate('general.no', 'FWC', 'No') + '</b>',
                                inputValue: 0
                            }
                        ]
                    },
                    {
                        xtype: 'checkboxgroup',
                        fieldLabel: Uni.I18n.translate('deviceType.firmwareupgradeoptions.options', 'FWC', 'Firmware upgrade options'),
                        required: true,
                        itemId: 'firmwareUpgradeOptions',
                        columns: 1,
                        vertical: true,
                        defaults: {
                            name: 'allowedOptions',
                            getModelData: function () {
                                return this.getSubmitData();
                            }
                        },
                        items: [
                            {
                                itemId: 'firmwareUpgradeOptionsLater',
                                boxLabel: '<b>' + Uni.I18n.translate('general.upload.fw.later', 'FWC', 'Upload firmware and activate later') + '</b>',
                                inputValue: 'install',
                                afterSubTpl: '<span style="color: grey;padding: 0 0 0 19px;">' + Uni.I18n.translate('general.upload.fw.later.comment', 'MDC', 'Firmware will be uploaded to the device. The user will need to send an command afterwards in order to activate firmware') + '</span>'
                            },
                            {
                                itemId: 'firmwareUpgradeOptionsImmediately',
                                boxLabel: '<b>' + Uni.I18n.translate('general.upload.fw.immediately', 'FWC', 'Upload firmware and activate immediately') + '</b>',
                                inputValue: 'activate',
                                afterSubTpl: '<span style="font-style:italic;color: grey;padding: 0 0 0 19px;">' + Uni.I18n.translate('general.upload.fw.immediately.comment', 'FWC', 'Firmware will be activated as soon as it is uploaded to the device') + '</span>'
                            },
                            {
                                itemId: 'firmwareUpgradeOptionsOnDate',
                                boxLabel: '<b>' + Uni.I18n.translate('general.upload.fw.ondate', 'FWC', 'Upload firmware with activation date') + '</b>',
                                inputValue: 'activateOnDate',
                                afterSubTpl: '<span style="font-style:italic;color: grey;padding: 0 0 0 19px;">' + Uni.I18n.translate('general.upload.fw.ondate.comment', 'FWC', 'Firmware will be uploaded to the device. Firmware will be activated at date and time specified by user') + '</span>'
                            }
                        ]
                    },
                    {
                        xtype: 'fieldcontainer',
                        ui: 'actions',
                        fieldLabel: '&nbsp',
                        layout: {
                            type: 'hbox'
                        },
                        items: [
                            {
                                text: Uni.I18n.translate('general.add', 'MDC', 'Save'),
                                xtype: 'button',
                                ui: 'action',
                                action: 'saveOptionsAction',
                                itemId: 'saveButton',
                                record: this.record,
                                router: this.router
                            },
                            {
                                text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel'),
                                xtype: 'button',
                                ui: 'link',
                                itemId: 'cancelLink',
                                href: '#/administration/devicetypes/' + this.deviceType.data.id + '/firmware/options'
                            }
                        ]
                    }
                ]
            }
        ];

        this.callParent(arguments);
    }
});



