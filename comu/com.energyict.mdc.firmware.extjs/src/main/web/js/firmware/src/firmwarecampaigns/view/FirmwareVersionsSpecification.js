/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Fwc.firmwarecampaigns.view.FirmwareVersionsSpecification', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.firmware-specifications',
    itemId: 'firmware-specifications',
    requires: [
        'Fwc.view.firmware.FirmwareOptionsXTemplate',
        'Fwc.firmwarecampaigns.model.FirmwareCampaign'
    ],
    deviceType: null,
    model: null,

    initComponent: function () {
        var me = this;

        me.content = [
            {
                ui: 'medium',
                layout: 'hbox',
                itemId: 'management-container',
                title: '',
                items: [
                    {
                        xtype: 'form',
                        padding: '15 0 0 0',
                        itemId: 'form',
                        store: 'Fwc.firmwarecampaigns.model.FirmwareCampaign',
                        flex: 1,
                        defaults: {
                            labelWidth: 250
                        },
                        items: [
                            {
                                xtype: 'displayfield',
                                name: 'targetFirmwareCheck',
                                itemId: 'target-options',
                                fieldLabel: Uni.I18n.translate('general.targetManagementOptions', 'FWC', 'Target firmware status'),
                                renderer: function (value, field) {
                                    var result = '',
                                        record = field.up('form').getRecord();
                                    if (!record) {
                                        field.hide();
                                    } else {
                                        var targetFirmwareCheck = value;

                                        if (!targetFirmwareCheck) {
                                            field.hide();
                                            return;
                                        }
                                        field.show();

                                        if (targetFirmwareCheck) {
                                            var targetFirmwareOptionTemplate = [];
                                            var targetFirmwareOptionsValues = {
                                                'FINAL': Uni.I18n.translate('general.targetFirmwareFinalOption', 'FWC', 'Final status of target firmware'),
                                                'TEST': Uni.I18n.translate('general.targetFirmwareTestOption', 'FWC', 'Test status of target firmware')
                                            };
                                            targetFirmwareCheck.sort();
                                            targetFirmwareCheck.forEach(function (item) {
                                                targetFirmwareOptionTemplate.push({"localizedValue": targetFirmwareOptionsValues[item]});
                                            })
                                            if (targetFirmwareOptionTemplate && targetFirmwareOptionTemplate.length) {
                                                var tpl = Ext.create('FirmwareOptionsXTemplate');
                                                result += ('<div style="margin:0 0 0 -3px">' + tpl.apply(targetFirmwareOptionTemplate) + '</div>');
                                            }
                                        }
                                    }
                                    return result;
                                }
                            },
                            {
                                xtype: 'displayfield',
                                name: 'curFirmwareCheck',
                                itemId: 'cur-options',
                                fieldLabel: Uni.I18n.translate('general.rankManagementOptions', 'FWC', 'Dependencies check'),
                                renderer: function (value, field) {
                                    var result = '',
                                        record = field.up('form').getRecord();
                                    if (!record) {
                                        field.hide();
                                    } else {
                                        var currentFirmwareCheck = value;

                                        if (!currentFirmwareCheck || !currentFirmwareCheck.length) {
                                            field.hide();
                                            return;
                                        }
                                        field.show();

                                        result = Uni.I18n.translate('general.upload.fw.currentFirmwareCheck', 'FWC', "The target firmware version should have a higher rank than the current firmware version on the device with the same type");

                                        var me = this;
                                    }
                                    return result ? result : "-";
                                },
                                listeners: {
                                    afterrender: function () {
                                        var me = this;
                                        /*this.el.hover(function(e){
                                            if (this.querySelector("div").getAttribute("data-qtip")) this.querySelector("div").removeAttribute("data-qtip");
                                            me.el.removeAllListeners();
                                        });*/
                                    }
                                }
                            },
                            {
                                xtype: 'displayfield',
                                name: 'masterFirmwareCheck',
                                itemId: 'master-options',
                                fieldLabel: ' ',
                                renderer: function (value, field) {
                                    var result = '',
                                        record = field.up('form').getRecord();
                                    if (!record) {
                                        field.hide();
                                    } else {
                                        var masterFirmwareCheck = value;

                                        if (!masterFirmwareCheck) {
                                            field.hide();
                                            return;
                                        }
                                        field.show();

                                        if (masterFirmwareCheck) {
                                            var masterFirmwareOptionTemplate = [];
                                            var masterFirmwareOptionsValues = {
                                                'FINAL': Uni.I18n.translate('general.upload.fw.masterFirmwareCheckFinalOption', 'FWC', 'Final status of firmware on master device'),
                                                'TEST': Uni.I18n.translate('general.upload.fw.masterFirmwareCheckTestOption', 'FWC', 'Test status of firmware on master device')
                                            };
                                            masterFirmwareCheck.sort();
                                            masterFirmwareCheck.forEach(function (item) {
                                                masterFirmwareOptionTemplate.push({"localizedValue": masterFirmwareOptionsValues[item]});
                                            })
                                            if (masterFirmwareOptionTemplate && masterFirmwareOptionTemplate.length) {
                                                result += '<div style="margin:10 0px">' + Uni.I18n.translate('general.upload.fw.masterFirmwareCheck', 'FWC', 'Master has the latest firmware (meter, communication and auxiliary)') + '</div>';
                                                var tpl = Ext.create('FirmwareOptionsXTemplate');
                                                result += ('<div style="margin:0 0 10px 30px">' + tpl.apply(masterFirmwareOptionTemplate) + '</div>');
                                            }
                                        }
                                    }
                                    return result ? result : "-";
                                }
                            }
                        ]
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});
