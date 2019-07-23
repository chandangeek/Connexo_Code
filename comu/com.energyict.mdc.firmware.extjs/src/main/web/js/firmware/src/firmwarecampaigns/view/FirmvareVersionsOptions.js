/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Fwc.firmwarecampaigns.view.FirmvareVersionsOptions', {
    extend: 'Ext.form.FieldContainer',
    alias: 'widget.firmware-version-options',
    required: true,
    layout: {
        type: 'vbox',
        align: 'left'
    },
    store: 'Fwc.firmwarecampaigns.store.FirmvareVersionsOptions',

    initComponent: function () {
        var me = this;

        me.items = [{
                xtype: 'checkboxgroup',
                required: false,
                itemId: 'firmwareTargetFileStatus',
                columns: 1,
                vertical: true,
                fieldLabel: Uni.I18n.translate('general.firmwareTargetFileStatus', 'FWC', 'Target firmware status'),
                items: [
                    {
                        beforeSubTpl: '<span style="font-style:italic;color: grey;padding: 0 5px 5px 0;">' + Uni.I18n.translate('general.upload.fw.target.firm.status', 'FWC', 'Check if the uploaded firmware has this status') + '</span>',
                        itemId: 'targetFirmwareCheckFinal',
                        boxLabel: Uni.I18n.translate('general.upload.fw.targetFirmwareCheckFinalOption', 'FWC', 'Final status of target firmware'),
                        inputValue: 'targetFirmwareCheckFinal',
                    },
                    {
                        itemId: 'targetFirmwareCheckTest',
                        boxLabel: Uni.I18n.translate('general.upload.fw.targetFirmwareCheckTestOption', 'FWC', 'Test status of target firmware'),
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
                fieldLabel: Uni.I18n.translate('general.firmwareDependenciesCheck', 'FWC', 'Dependencies check'),
                required: false,
                itemId: 'dependenciesCheckTargetOption',
                columns: 1,
                vertical: true,
                items: [
                    {
                        itemId: 'curFirmwareCheck',
                        boxLabel: '<b>' + Uni.I18n.translate('general.upload.fw.currentFirmwareCheck', 'FWC', 'The target firmware version should have a higher rank than the current firmware version on the device with the same type. All firmware types present in the device should have a rank not less than that of the version with the minimal level configured on the target version') + '</b>',
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
                        boxLabel: '<b>' + Uni.I18n.translate('general.upload.fw.masterFirmwareCheck', 'FWC', 'Master has the latest firmware (both meter and communication)') + '</b>',
                        inputValue: 'currentFirmwareCheck',
                        afterSubTpl: '<span style="font-style:italic;color: grey;padding: 0 0 0 19px;">' + Uni.I18n.translate('general.upload.fw.masterFirmwareCheck.comment', 'FWC', 'The latest firmeware on the master is chosen only within versions with the selected status') + '</span>',
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
                        boxLabel: Uni.I18n.translate('general.upload.fw.targetFirmwareCheckTestOption', 'FWC', 'Test status of firmware on master device'),
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
            }]

            me.callParent(arguments);
        }

    });
