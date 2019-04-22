/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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
                ui: 'medium',
                layout: 'hbox',
                itemId: 'management-container',
                title: Uni.I18n.translate('general.firmwareManagement', 'FWC', 'Firmware management'),
                tools: [
                    {
                        xtype: 'uni-button-action',
                        itemId: 'fwc-specifications-actions-btn',
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
                                    if (record && record.get('supportedOptions').length === 0) {
                                        result = Uni.I18n.translate('deviceType.firmwaremanagementoptions.notAllowedByProtocol', 'FWC',
                                            'Firmware management is not supported by the communication protocol of this device type'
                                        );
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
                                renderer: function (value, field) {
                                    var result = '',
                                        record = field.up('form').getRecord();
                                    if (record && record.get('supportedOptions').length === 0) {
                                        field.hide();
                                    } else {
                                        field.show();
                                        var tpl = Ext.create('FirmwareOptionsXTemplate');
                                        result = tpl.apply(value);
                                    }
                                    return result;
                                }
                            },
                            {
                                xtype: 'displayfield',
                                name: 'targetOptions',
                                itemId: 'target-options',
                                fieldStyle: 'margin-top : 3px;',
                                fieldLabel: Uni.I18n.translate('general.targetManagementOptions', 'FWC', 'Target firmware status'),
                                renderer: function (value, field) {
                                      var result = '',
                                          record = field.up('form').getRecord();
                                      if (!record || record.get('isAllowed') === false) {
                                          field.hide();
                                      } else {
                                              var targetFirmwareCheck = value;

                                              if  (!targetFirmwareCheck) {
                                                  field.hide();
                                                  return;
                                              }
                                              field.show();

                                              if (targetFirmwareCheck && targetFirmwareCheck.activated){
                                                  var targetFirmwareOptionTemplate = [];
                                                  var targetFirmwareOptionsValues = {
                                                      'FINAL' : Uni.I18n.translate('general.targetFirmwareFinalOption', 'FWC', 'Final status of the target firmware'),
                                                      'TEST' :  Uni.I18n.translate('general.targetFirmwareTestOption', 'FWC', 'Test status of the target firmware')
                                                  };
                                                  targetFirmwareCheck['statuses'].sort();
                                                  targetFirmwareCheck['statuses'].forEach(function(item){
                                                      targetFirmwareOptionTemplate.push({"localizedValue" : targetFirmwareOptionsValues[item]});
                                                  })
                                                  if (targetFirmwareOptionTemplate && targetFirmwareOptionTemplate.length){
                                                      var tpl = Ext.create('FirmwareOptionsXTemplate');
                                                      result += ('<div style="margin:0 0 10px -3px">' + tpl.apply(targetFirmwareOptionTemplate) + '</div>');
                                                  }
                                              }
                                      }
                                    return result;
                                }
                            },
                            {
                                xtype: 'displayfield',
                                name: 'checkOptions',
                                itemId: 'checked-options',
                                fieldStyle: 'margin-top : 3px;',
                                fieldLabel: Uni.I18n.translate('general.rankManagementOptions', 'FWC', 'Dependencies check'),
                                renderer: function (value, field) {
                                    var result = '',
                                        record = field.up('form').getRecord();
                                    if (record && (record.get('checkOptions').length === 0 || record.get('isAllowed') === false)) {
                                        field.hide();
                                    } else {
                                        var currentFirmwareCheck  = value && value["CURRENT_FIRMWARE_CHECK"];
                                        var masterFirmwareCheck = value && value["MASTER_FIRMWARE_CHECK"];

                                        if  (!masterFirmwareCheck && !currentFirmwareCheck) {
                                            field.hide();
                                            return;
                                        }
                                        field.show();

                                        if (currentFirmwareCheck && currentFirmwareCheck.activated){
                                            var currentFirmwareOptionTemplate = [{
                                                "localizedValue" : Uni.I18n.translate('general.targetFirmwareFinalOption', 'FWC', "The target firmware version should have a higher rank than the current firmware version on the device with the same type. All firmware types present in the device should have a rank not less than that of the version with the minimal level configured on the target version")
                                            }];

                                            var tpl = Ext.create('FirmwareOptionsXTemplate');
                                            result += ('<div style="margin:0 0 10px -3px">' + tpl.apply(currentFirmwareOptionTemplate) + '</div>');
                                        }

                                        if (masterFirmwareCheck && masterFirmwareCheck.activated){
                                            var masterFirmwareOptionTemplate = [];
                                            var masterFirmwareOptionsValues = {
                                                       'FINAL' : Uni.I18n.translate('general.masterFirmwareFinalOption', 'FWC', 'Final status of firmware on master device'),
                                                       'TEST' : Uni.I18n.translate('general.masterFirmwareTestOption', 'FWC', 'Test status of firmware on master device')
                                                    };
                                            masterFirmwareCheck['statuses'].sort();
                                            masterFirmwareCheck['statuses'].forEach(function(item){
                                                masterFirmwareOptionTemplate.push({"localizedValue" : masterFirmwareOptionsValues[item]});
                                            })
                                            if (masterFirmwareOptionTemplate && masterFirmwareOptionTemplate.length){
                                                result += '<div style="margin-bottom:10px">' + Uni.I18n.translate('general.masterFirmwareMainOption', 'FWC', 'Master has a latest firmware(both meter and communication)') + '</div>';
                                                var tpl = Ext.create('FirmwareOptionsXTemplate');
                                                result += ('<div style="margin-bottom:10px">' + tpl.apply(masterFirmwareOptionTemplate) + '</div>');
                                            }
                                        }
                                    }
                                    return result ? result : "-";
                                }
                            }
                        ],
                        loadRecord: function (record) {
                            this.getForm().loadRecord(record);
                            if (record.get('supportedOptions').length === 0) {
                                me.down('#fwc-specifications-actions-btn').disable();
                            }
                        }
                    }
                ]
            },
            {
                ui: 'medium',
                layout: 'hbox',
                itemId: 'security-check-container',
                title: Uni.I18n.translate('general.firmwareSignatureCheck', 'FWC', 'Firmware signature check'),
                tools: [
                    {
                        xtype: 'uni-button-action',
                        itemId: 'fwc-specifications-signature-actions-btn',
                        menu: {
                            plain: true,
                            border: false,
                            shadow: false,
                            items: [
                                {
                                    itemId: 'mdc-edit-options-btn',
                                    text: Uni.I18n.translate('general.edit', 'FWC', 'Edit'),
                                    privileges: Mdc.privileges.SecurityAccessor.admin,
                                    action: 'editFirmwareOptionsSecurityAccessor'
                                },
                                {
                                    itemId: 'mdc-clear-accessor-options-btn',
                                    text: Uni.I18n.translate('general.clearAccessor', 'FWC', 'Clear accessor'),
                                    privileges: Mdc.privileges.SecurityAccessor.admin,
                                    action: 'clearAccessorFirmwareSignatureOptions'
                                }
                            ]
                        }
                    }
                ],
                items: [
                    {
                        xtype: 'form',
                        padding: '15 0 0 0',
                        itemId: 'security-check-form',
                        flex: 1,
                        defaults: {
                            labelWidth: 250
                        },
                        items: [
                            {
                                xtype: 'displayfield',
                                name: 'name',
                                itemId: 'security-accessor',
                                fieldLabel: Uni.I18n.translate('deviceType.firmwaremanagementoptions.securityAccessor', 'FWC', 'Security accessor')
                            },
                            {
                                xtype: 'displayfield',
                                name: 'type',
                                itemId: 'certificate-type',
                                fieldLabel: Uni.I18n.translate('deviceType.firmwaremanagementoptions.certificateType', 'FWC', 'Certificate type')
                            },
                            {
                                xtype: 'displayfield',
                                name: 'truststore',
                                itemId: 'trust-store',
                                fieldLabel: Uni.I18n.translate('deviceType.firmwaremanagementoptions.trustStore', 'FWC', 'Trust store')
                            },
                            {
                                xtype: 'displayfield',
                                name: 'certificate',
                                itemId: 'certificate',
                                fieldLabel: Uni.I18n.translate('deviceType.firmwaremanagementoptions.certificate', 'FWC', 'Certificate')
                            },
                            {
                                xtype: 'displayfield',
                                name: 'expirationTime',
                                itemId: 'valid-until',
                                fieldLabel: Uni.I18n.translate('deviceType.firmwaremanagementoptions.validUntil', 'FWC', 'Valid until')
                            }
                        ]
                    }
                ],
                loadRecord: function (record) {
                    var securityCheckForm = me.down('#security-check-form');
                    if (!record.get('id')) {
                        securityCheckForm.down('#certificate-type').hide();
                        securityCheckForm.down('#trust-store').hide();
                        securityCheckForm.down('#certificate').hide();
                        securityCheckForm.down('#valid-until').hide();
                    } else {
                        securityCheckForm.loadRecord(record);
                    }
                }
            }


        ];

        me.callParent(arguments);
    }
});


