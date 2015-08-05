Ext.define('Fwc.view.firmware.FirmwareOptionsEdit', {
    extend: 'Uni.view.container.ContentContainer',
    requires: [
        'Fwc.model.FirmwareManagementOptions'
    ],
    alias: 'widget.firmware-options-edit',
    itemId: 'firmware-options-edit',
    deviceType: null,

    initComponent: function () {
        this.content = [
            {
                xtype: 'form',
                title: Uni.I18n.translate('deviceType.firmwaremanagemenoptions.edit', 'FWC', 'Edit firmware management options'),
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
                    var checkboxgroup = this.down('#firmwareUpgradeOptions'),
                        radiogroup = this.down('#allowedRadioGroup');

                    this.getForm().loadRecord(record);

                    if (!record.get('isAllowed')) {
                        radiogroup.setValue({'isAllowed': 0});
                        checkboxgroup.disable();
                        checkboxgroup.setValue([]);
                    }

                    // remove the unsupported options.
                    checkboxgroup.items.each(function (item) {
                        if (record.get('supportedOptions')
                            .map(function (option) {
                                return option.id;
                            })
                            .indexOf(item.inputValue) < 0) {
                            checkboxgroup.remove(item);
                            item.submitValue = false;
                        } else {
                            item.setValue(record.get('allowedOptions')
                                .map(function (option) {
                                    return option.id;
                                })
                                .indexOf(item.inputValue) >= 0);
                        }

                    });

                    radiogroup.on('change', function (radiogroup, newValue) {
                        var form = radiogroup.up('form'),
                            checkboxgroup = form.down('#firmwareUpgradeOptions');

                        if (!newValue.isAllowed) {
                            checkboxgroup.disable();
                            checkboxgroup.setValue([]);
                        } else {
                            checkboxgroup.enable();
                            Ext.each(checkboxgroup.items.items, function (checkbox) {
                                checkbox.setValue(true);
                            });
                        }
                    }, radiogroup);
                },
                updateRecord: function () {
                    this.getForm().updateRecord();
                    var record = this.getForm().getRecord();
                },
                items: [
                    {
                        xtype: 'uni-form-error-message',
                        itemId: 'form-errors',
                        margin: '0 0 10 0',
                        hidden: true
                    },
                    {
                        xtype: 'radiogroup',
                        fieldLabel: Uni.I18n.translate('deviceType.firmwaremanagementoptions.allowed', 'FWC', 'Firmware management allowed'),
                        itemId: 'allowedRadioGroup',
                        required: true,
                        columns: 1,
                        vertical: true,
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
                        fieldLabel: Uni.I18n.translate('deviceType.firmwaremanagementoptions.options', 'FWC', 'Firmware management options'),
                        required: true,
                        itemId: 'firmwareUpgradeOptions',
                        columns: 1,
                        vertical: true,
                        defaults: {
                            name: 'selectedOptions',
                            getModelData: function () {
                                return this.getSubmitData();
                            }

                        },
                        items: [
                            {
                                itemId: 'firmwareUpgradeOptionsLater',
                                boxLabel: '<b>' + Uni.I18n.translate('general.upload.fw.later', 'FWC', 'Upload firmware and activate later') + '</b>',
                                inputValue: 'install',
                                afterSubTpl: '<span style="font-style:italic;color: grey;padding: 0 0 0 19px;">' + Uni.I18n.translate('general.upload.fw.later.comment', 'MDC', 'Firmware will be uploaded to the device. The user will need to send an command afterwards in order to activate firmware') + '</span>'
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
                        itemId: 'allowedOptionsError',
                        fieldLabel: '&nbsp'
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
                                itemId: 'saveButton'
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



