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
                        allowedCheckBox = this.down('#fwc-firmware-options-edit-firmware-checkbox'),
                        me = this;

                    this.getForm().loadRecord(record);

                    function showAllCheckboxes(isShown) {
                        var allOptions = ['dependenciesCheckTargetOption', 'targetFirmwareCheckFinal', 'targetFirmwareCheckTest', 'masterFirmwareCheck', 'masterFirmwareCheckFinal', 'masterFirmwareCheckTest'];
                        allOptions.forEach(function (item) {
                            var option = me.down('#' + item);
                            if (option) {
                                isShown ? option.enable() : option.disable();
                            }
                        })
                    }

                    if (!record.get('isAllowed')) {
                        allowedCheckBox.setValue(false);
                        checkboxgroup.disable();
                        checkboxgroup.setValue([]);
                        showAllCheckboxes(false);
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

                        showAllCheckboxes(newValue);

                        if (newValue) {
                            var masterFirmwareMainOption = me.down('#masterFirmwareCheck');
                            if (masterFirmwareMainOption) {
                                masterFirmwareMainOption.setValue(true);
                                var masterFirmwareCheckFinal = me.down('#masterFirmwareCheckFinal');
                                var masterFirmwareCheckTest = me.down('#masterFirmwareCheckTest');
                                if (masterFirmwareCheckFinal) masterFirmwareCheckFinal.setValue(true);
                                if (masterFirmwareCheckTest) masterFirmwareCheckTest.setValue(false);
                            }

                            var curFirmwareCheck = me.down('#curFirmwareCheck');
                            if (curFirmwareCheck) curFirmwareCheck.setValue(true);

                            var targetFirmwareCheckFinal = me.down('#targetFirmwareCheckFinal');
                            var targetFirmwareCheckTest = me.down('#targetFirmwareCheckTest');

                            if (targetFirmwareCheckFinal) targetFirmwareCheckFinal.setValue(true);
                            if (targetFirmwareCheckTest) targetFirmwareCheckTest.setValue(false);
                        }
                    }, allowedCheckBox);

                    var dependenciesCheckOptionsData = record.data['checkOptions'];


                    function setVisibleCheckBoxes(mainOptionId, finalOptionId, testOptionId, modelData) {
                        var mainOption = me.down('#' + mainOptionId);
                        var finalOption = me.down('#' + finalOptionId);
                        var testOption = me.down('#' + testOptionId);

                        if (modelData) {

                            mainOption.show();

                            if (modelData['statuses'] && (modelData['statuses'] instanceof Array)) {

                                finalOptionVal = modelData['statuses'].indexOf('FINAL') !== -1;
                                testOptionVal = modelData['statuses'].indexOf('TEST') !== -1;
                                if (finalOptionVal || testOptionVal) {
                                    mainOption.setValue(true);
                                } else {
                                    finalOption.disable();
                                    testOption.disable();
                                }
                                finalOption.setValue(finalOptionVal);
                                testOption.setValue(testOptionVal);

                            }

                            finalOption.on('change', function (checkBox, newVal, oldVal) {
                                if (newVal === oldVal) return;
                                if (newVal) mainOption.setValue(newVal);
                            });

                            mainOption.on('change', function (checkBox, newVal, oldVal) {
                                if (newVal === oldVal) return;
                                if (newVal) {
                                    finalOption.enable();
                                    finalOption.setValue(true);
                                    testOption.enable();
                                } else {
                                    finalOption.setValue(false);
                                    finalOption.disable();
                                    testOption.setValue(false);
                                    testOption.disable();
                                }
                            });

                        } else {
                            mainOption.hide();
                            finalOption.hide();
                            testOption.hide()
                        }
                    }

                    if (dependenciesCheckOptionsData) {
                        setVisibleCheckBoxes('masterFirmwareCheck', 'masterFirmwareCheckFinal', 'masterFirmwareCheckTest', dependenciesCheckOptionsData['MASTER_FIRMWARE_CHECK']);
                        var curFirmwareCheckOption = me.down('#curFirmwareCheck');
                        if (curFirmwareCheckOption) {
                            if (dependenciesCheckOptionsData['CURRENT_FIRMWARE_CHECK']) {
                                curFirmwareCheckOption.show();
                                curFirmwareCheckOption.setValue(dependenciesCheckOptionsData['CURRENT_FIRMWARE_CHECK'].activated);
                            } else {
                                curFirmwareCheckOption.hide();
                            }
                        }
                        var targetFirmwareCheckOption = me.down('#firmwareTargetFileStatus');
                        if (targetFirmwareCheckOption) {
                            var data = dependenciesCheckOptionsData['TARGET_FIRMWARE_STATUS_CHECK'];
                            if (data) {
                                targetFirmwareCheckOption.show();
                                targetFirmwareCheckOption.setValue(data.activated);
                                if (!data['statuses'] || !(data['statuses'] instanceof Array)) return;
                                var finalOptionVal = data['statuses'].indexOf('FINAL') !== -1;
                                me.down('#targetFirmwareCheckFinal').setValue(finalOptionVal);
                                var testOptionVal = data['statuses'].indexOf('TEST') !== -1;
                                me.down('#targetFirmwareCheckTest').setValue(testOptionVal);
                            } else {
                                targetFirmwareCheckOption.hide();
                            }
                        }

                    }

                },
                updateRecord: function () {
                    this.getForm().updateRecord();
                    var record = this.getForm().getRecord();
                    var checkOptions = {};

                    var result = true;

                    var curFirmwareCheckOption = me.down('#curFirmwareCheck');

                    var masterFirmwareCheckFinal = this.down("#masterFirmwareCheckFinal");
                    var masterFirmwareCheckTest = this.down("#masterFirmwareCheckTest");
                    var masterFirmwareMainOption = me.down('#masterFirmwareCheck');

                    var targetFirmwareCheckFinal = this.down("#targetFirmwareCheckFinal");
                    var targetFirmwareCheckTest = this.down("#targetFirmwareCheckTest");

                    checkOptions["CURRENT_FIRMWARE_CHECK"] = {};
                    checkOptions["CURRENT_FIRMWARE_CHECK"]["statuses"] = [];
                    checkOptions["CURRENT_FIRMWARE_CHECK"]["activated"] = curFirmwareCheckOption.getValue() ? true : false;

                    checkOptions["TARGET_FIRMWARE_STATUS_CHECK"] = {};
                    checkOptions["TARGET_FIRMWARE_STATUS_CHECK"]["activated"] = targetFirmwareCheckFinal.getValue() || targetFirmwareCheckTest.getValue() ? true : false;
                    var targetOptions = checkOptions["TARGET_FIRMWARE_STATUS_CHECK"]["statuses"] = [];
                    if (targetFirmwareCheckFinal && targetFirmwareCheckFinal.getValue()) targetOptions.push("FINAL");
                    if (targetFirmwareCheckTest && targetFirmwareCheckTest.getValue()) targetOptions.push("TEST");
                    if (!targetOptions.length) {
                        me.down('#firmwareTargetOptionsError').show();
                        result = false;
                    } else {
                        me.down('#firmwareTargetOptionsError').hide();
                    }


                    checkOptions["MASTER_FIRMWARE_CHECK"] = {};
                    checkOptions["MASTER_FIRMWARE_CHECK"]["activated"] = masterFirmwareCheckFinal.getValue() || masterFirmwareCheckTest.getValue() ? true : false;
                    var masterOptions = checkOptions["MASTER_FIRMWARE_CHECK"]["statuses"] = [];
                    if (masterFirmwareCheckFinal && masterFirmwareCheckFinal.getValue()) masterOptions.push("FINAL");
                    if (masterFirmwareCheckTest && masterFirmwareCheckTest.getValue()) masterOptions.push("TEST");
                    if (masterFirmwareMainOption.getValue() && !masterOptions.length) {
                        me.down('#masterOptionsError').show();
                        result = false;
                    } else {
                        me.down('#masterOptionsError').hide();
                    }
                    if (result) record.set("checkOptions", checkOptions);

                    var firmwareUpgradeOptions = me.down('#firmwareUpgradeOptions');
                    if (!firmwareUpgradeOptions.getValue() && !firmwareUpgradeOptions.getValue().selectedOptions) {
                        me.down('#firmwareUpgradeOptionsError').show();
                        result = false;
                    } else {
                        me.down('#firmwareUpgradeOptionsError').hide();
                    }

                    return result;
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
                        xtype: 'displayfield',
                        itemId: 'firmwareUpgradeOptionsError',
                        padding: '-10 0 -10 0',
                        fieldLabel: '&nbsp',
                        hidden: true,
                        renderer: function (value, field) {
                            return '<span style="color:red;">' + Uni.I18n.translate('firmware.specs.save.validationError', 'FWC', 'You must select at least one item in the group') + '</span>';
                        }
                    },
                    {
                        xtype: 'checkboxgroup',
                        itemId: 'firmwareTargetFileStatus',
                        columns: 1,
                        vertical: true,
                        fieldLabel: Uni.I18n.translate('general.targetManagementOptions', 'FWC', 'Target firmware status'),
                        required: true,
                        items: [
                            {
                                beforeSubTpl: '<span style="font-style:italic;color: grey;padding: 0 5px 5px 0;">' + Uni.I18n.translate('general.upload.fw.target.firm.status', 'FWC', 'Check if the uploaded firmware has this status') + '</span>',
                                itemId: 'targetFirmwareCheckFinal',
                                boxLabel: Uni.I18n.translate('general.targetFirmwareFinalOption', 'FWC', 'Final status of target firmware'),
                                inputValue: 'targetFirmwareCheckFinal',
                            },
                            {
                                itemId: 'targetFirmwareCheckTest',
                                boxLabel: Uni.I18n.translate('general.targetFirmwareTestOption', 'FWC', 'Test status of target firmware'),
                                inputValue: 'targetFirmwareCheckTest',
                            }
                        ]
                    },
                    {
                        xtype: 'displayfield',
                        itemId: 'firmwareTargetOptionsError',
                        padding: '-10 0 -10 0',
                        fieldLabel: '&nbsp',
                        hidden: true,
                        renderer: function (value, field) {
                            return '<span style="color:red;">' + Uni.I18n.translate('firmware.specs.save.validationError', 'FWC', 'You must select at least one item in the group') + '</span>';
                        }
                    },
                    {
                        xtype: 'checkboxgroup',
                        fieldLabel: Uni.I18n.translate('general.rankManagementOptions', 'FWC', 'Dependencies check'),
                        required: false,
                        itemId: 'dependenciesCheckTargetOption',
                        columns: 1,
                        vertical: true,
                        items: [
                            {
                                itemId: 'curFirmwareCheck',
                                boxLabel: '<b>' + Uni.I18n.translate('general.upload.fw.currentFirmwareCheck', 'FWC', 'The target firmware version should have a higher rank than the current firmware version on the device with the same type') + '</b>',
                                inputValue: 'curFirmwareCheck'
                            }
                        ]
                    },
                    {
                        xtype: 'checkboxgroup',
                        fieldLabel: ' ',
                        required: false,
                        itemId: 'masterFirmwareMainOption',
                        columns: 1,
                        vertical: true,
                        items: [
                            {
                                itemId: 'masterFirmwareCheck',
                                boxLabel: '<b>' + Uni.I18n.translate('general.upload.fw.masterFirmwareCheck', 'FWC', 'Master has the latest firmware (meter, communication and auxiliary)') + '</b>',
                                inputValue: 'currentFirmwareCheck',
                                afterSubTpl: '<span style="font-style:italic;color: grey;padding: 0 0 0 19px;">' + Uni.I18n.translate('general.upload.fw.masterFirmwareCheck.comment', 'FWC', 'The latest firmware on the master is chosen only within versions with the selected status') + '</span>',
                            }
                        ]
                    },
                    {
                        xtype: 'checkboxgroup',
                        required: false,
                        itemId: 'masterFirmwareCheckOptions',
                        columns: 1,
                        vertical: true,
                        fieldLabel: ' ',
                        margin: '0 0 0 30',
                        items: [
                            {
                                itemId: 'masterFirmwareCheckFinal',
                                boxLabel: Uni.I18n.translate('general.upload.fw.masterFirmwareCheckFinalOption', 'FWC', 'Final status of firmware on master device'),
                                inputValue: 'masterFirmwareCheckFinal',
                            },
                            {
                                itemId: 'masterFirmwareCheckTest',
                                boxLabel: Uni.I18n.translate('general.upload.fw.masterFirmwareCheckTestOption', 'FWC', 'Test status of firmware on master device'),
                                inputValue: 'masterFirmwareCheckTest',
                            }
                        ]
                    },
                    {
                        xtype: 'displayfield',
                        itemId: 'masterOptionsError',
                        fieldLabel: '&nbsp',
                        hidden: true,
                        renderer: function (value, field) {
                            return '<span style="color:red;margin:10px 0 0 30px;">' + Uni.I18n.translate('firmware.specs.save.validationError', 'FWC', 'You must select at least one item in the group') + '</span>';
                        }
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



