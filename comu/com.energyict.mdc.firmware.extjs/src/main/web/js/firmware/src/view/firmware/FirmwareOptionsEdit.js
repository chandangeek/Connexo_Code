/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Fwc.view.firmware.FirmwareOptionsEdit', {
    extend: 'Uni.view.container.ContentContainer',
    requires: [
        'Fwc.model.FirmwareManagementOptions'
    ],
    alias: 'widget.firmware-options-edit',
    itemId: 'firmware-options-edit',
    deviceType: null,

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

        this.content = [
            {
                xtype: 'form',
                title: Uni.I18n.translate('general.firmwareManagementSpecifications.edit', 'FWC', 'Edit firmware management specifications'),
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
                        allowedCheckBox = this.down('#fwc-firmware-options-edit-firmware-checkbox');

                    this.getForm().loadRecord(record);

                    if (!record.get('isAllowed')) {
                        allowedCheckBox.setValue(false);
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

                    allowedCheckBox.on('change', function (checkBox, newValue) {
                        var form = checkBox.up('form'),
                            checkboxgroup = form.down('#firmwareUpgradeOptions');

                        if (!newValue) {
                            checkboxgroup.disable();
                            checkboxgroup.setValue([]);
                        } else {
                            checkboxgroup.enable();
                            Ext.each(checkboxgroup.items.items, function (checkbox) {
                                checkbox.setValue(true);
                            });
                        }
                    }, allowedCheckBox);
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
                        xtype: 'checkboxfield',
                        fieldLabel: Uni.I18n.translate('general.firmwareManagement', 'FWC', 'Firmware management'),
                        itemId: 'fwc-firmware-options-edit-firmware-checkbox',
                        name: 'isAllowed',
                        boxLabel: Uni.I18n.translate('general.allowFirmwareManagement', 'FWC', 'Allow firmware management'),
                        checked: false,
                        margin: '7 0 7 4'
                    },
                    {
                        xtype: 'checkboxgroup',
                        fieldLabel: Uni.I18n.translate('general.firmwareManagementOptions', 'FWC', 'Firmware management options'),
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
                                afterSubTpl: '<span style="font-style:italic;color: grey;padding: 0 0 0 19px;">' + Uni.I18n.translate('general.upload.fw.later.comment', 'FWC', 'Firmware will be uploaded to the device. The user will need to send an command afterwards in order to activate firmware') + '</span>'
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
                                text: Uni.I18n.translate('general.save', 'FWC', 'Save'),
                                xtype: 'button',
                                ui: 'action',
                                action: 'saveOptionsAction',
                                itemId: 'saveButton'
                            },
                            {
                                text: Uni.I18n.translate('general.cancel', 'FWC', 'Cancel'),
                                xtype: 'button',
                                ui: 'link',
                                itemId: 'cancelLink',
                                href: '#/administration/devicetypes/' + this.deviceType.data.id + '/firmwareversions'
                            }
                        ]
                    }
                ]
            }
        ];

        this.callParent(arguments);
    }
});



