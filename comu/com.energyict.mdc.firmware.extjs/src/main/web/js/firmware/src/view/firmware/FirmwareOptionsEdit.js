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

                    function showAllCheckboxes(isShown){
                        var allOptions = ['currentFirmwareCheck', 'currentFirmwareCheckFinal', 'currentFirmwareCheckTest', 'masterFirmwareCheck', 'masterFirmwareCheckFinal', 'masterFirmwareCheckTest'];
                        allOptions.forEach(function(item){
                           var option = me.down('#' + item);
                           if (option){
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
                        if (newValue){
                           var masterFirmwareMainOption = me.down('#masterFirmwareMainOption');
                           if (masterFirmwareMainOption){
                              masterFirmwareMainOption.setValue(true);
                              var masterFirmwareCheckFinal = me.down('#masterFirmwareCheckFinal');
                              if (masterFirmwareCheckFinal) masterFirmwareCheckFinal.setValue(true);
                           }
                        }
                    }, allowedCheckBox);

                    var dependenciesCheckOptionsData = record.data['checkOptions'];

                    var dependenciesMainOption = this.down('#dependenciesCheckMainOption');

                    function setVisibleCheckBoxes(mainOptionId, finalOptionId, testOptionId, modelData){
                       var mainOption =  me.down('#' + mainOptionId);
                       var finalOption =  me.down('#' + finalOptionId);
                       var testOption =  me.down('#' + testOptionId);

                       var mainOptionShown = modelData && modelData['activatedFor'];
                       if (mainOptionShown){
                           mainOption.show();

                           finalOption.setValue(modelData['activatedFor'].indexOf('FINAL') !==-1);
                           testOption.setValue(modelData['activatedFor'].indexOf('TEST') !==-1);

                           mainOption.handler = function(comp){
                              if (comp) {
                                  comp.value ? finalOption.setValue(true) : ( finalOption.setValue(false), testOption.setValue(false));
                              }
                           };

                       }
                       else{
                           mainOption.hide();
                           finalOption.hide();
                           testOption.hide()
                       }
                       return mainOptionShown;
                    }

                    if (dependenciesCheckOptionsData){
                      var currFirmBlockShown = setVisibleCheckBoxes('currentFirmwareCheck', 'currentFirmwareCheckFinal', 'currentFirmwareCheckTest', dependenciesCheckOptionsData['CURRENT_FIRMWARE_CHECK']);
                      var masterFirmBlockShown = setVisibleCheckBoxes('masterFirmwareCheck', 'masterFirmwareCheckFinal', 'masterFirmwareCheckTest', dependenciesCheckOptionsData['MASTER_FIRMWARE_CHECK']);
                      (currFirmBlockShown || masterFirmBlockShown) ? dependenciesMainOption.show() : dependenciesMainOption.hide();

                    }

                },
                updateRecord: function () {
                    this.getForm().updateRecord();
                    var record = this.getForm().getRecord();
                    var checkOptions = {};

                    var currentFirmwareCheck = this.down("#currentFirmwareCheck");
                    var currentFirmwareCheckFinal = this.down("#currentFirmwareCheckFinal");
                    var currentFirmwareCheckTest = this.down("#currentFirmwareCheckTest");

                    var masterFirmwareMainOption = this.down("#masterFirmwareCheck");
                    var masterFirmwareCheckFinal = this.down("#masterFirmwareCheckFinal");
                    var masterFirmwareCheckTest = this.down("#masterFirmwareCheckTest");

                    if ( currentFirmwareCheck && !currentFirmwareCheck.hidden){
                        checkOptions["CURRENT_FIRMWARE_CHECK"] = {};
                        var rankOptions = checkOptions["CURRENT_FIRMWARE_CHECK"]["activatedFor"] = [];
                        if (currentFirmwareCheckFinal && currentFirmwareCheckFinal.getValue()) rankOptions.push("FINAL");
                        if (currentFirmwareCheckTest && currentFirmwareCheckTest.getValue()) rankOptions.push("TEST");
                    }
                    if ( masterFirmwareMainOption && !masterFirmwareMainOption.hidden){
                        checkOptions["MASTER_FIRMWARE_CHECK"] = {};
                        var mOptions = checkOptions["MASTER_FIRMWARE_CHECK"]["activatedFor"] = [];
                        if (masterFirmwareCheckFinal && masterFirmwareCheckFinal.getValue()) mOptions.push("FINAL");
                        if (masterFirmwareCheckTest && masterFirmwareCheckTest.getValue()) mOptions.push("TEST");
                    }
                    record.set("checkOptions", checkOptions);
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
                        xtype: 'checkboxgroup',
                        fieldLabel: Uni.I18n.translate('general.firmwareDependenciesCheck', 'FWC', 'Dependencies check'),
                        required: false,
                        itemId: 'dependenciesCheckMainOption',
                        columns: 1,
                        vertical: true,
                        items: [
                            {
                                itemId: 'currentFirmwareCheck',
                                boxLabel: '<b>' + Uni.I18n.translate('general.upload.fw.currentFirmwareCheck', 'FWC', 'The target firmware version should have a higher rank than the current firmware version on the device with the same type. All firmware types present in the device should have a rank not less than that of the version with the minimal level configured on the target version') + '</b>',
                                inputValue: 'currentFirmwareCheck',
                                afterSubTpl: '<span style="font-style:italic;color: grey;padding: 0 0 0 19px;">' + Uni.I18n.translate('general.upload.fw.currentFirmwareCheck.comment', 'FWC', 'The check will be applied only to the target firmware with the selected status') + '</span>'
                            }
                        ]
                    },
                    {
                        xtype: 'checkboxgroup',
                        required: false,
                        itemId: 'dependenciesCheckOptions',
                        columns: 1,
                        vertical: true,
                        fieldLabel: ' ',
                        margin: '0 0 30 30',
                        items: [
                            {
                                itemId: 'currentFirmwareCheckFinal',
                                boxLabel: Uni.I18n.translate('general.upload.fw.currentFirmwareCheckFinalOption', 'FWC', 'Final status of target firmware'),
                                inputValue: 'currentFirmwareCheckFinal',
                            },
                            {
                                itemId: 'currentFirmwareCheckTest',
                                boxLabel: Uni.I18n.translate('general.upload.fw.currentFirmwareCheckTestOption', 'FWC', 'Test status of target firmware'),
                                inputValue: 'currentFirmwareCheckTest',
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
                                boxLabel: '<b>' + Uni.I18n.translate('general.upload.fw.masterFirmwareCheck', 'FWC', 'Master has the latest firmware (both meter and communication)') + '</b>',
                                inputValue: 'currentFirmwareCheck',
                                afterSubTpl: '<span style="font-style:italic;color: grey;padding: 0 0 0 19px;">' + Uni.I18n.translate('general.upload.fw.masterFirmwareCheck.comment', 'FWC', 'The check will be applied only to the target firmware with the selected status') + '</span>'
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
                                boxLabel: Uni.I18n.translate('general.upload.fw.masterFirmwareCheckFinalOption', 'FWC', 'Final status of target firmware on slave device'),
                                inputValue: 'masterFirmwareCheckFinal',
                            },
                            {
                                itemId: 'masterFirmwareCheckTest',
                                boxLabel: Uni.I18n.translate('general.upload.fw.currentFirmwareCheckTestOption', 'FWC', 'Test status of target firmware on slave device'),
                                inputValue: 'masterFirmwareCheckTest',
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



