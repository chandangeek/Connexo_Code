/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Fwc.view.firmware.FormEdit', {
    extend: 'Fwc.view.firmware.Form',
    xtype: 'firmware-form-edit',
    edit: true,
    hydrator: 'Fwc.form.Hydrator',
    items: [
        {
            xtype: 'uni-form-error-message',
            itemId: 'form-errors',
            margin: '0 0 10 0',
            anchor: '60%',
            hidden: true
        },
        {
            xtype: 'textfield',
            name: 'firmwareVersion',
            itemId: 'text-firmware-version',
            anchor: '60%',
            required: true,
            fieldLabel: Uni.I18n.translate('general.version', 'FWC', 'Version'),
            allowBlank: false
        },
        {
            xtype: 'firmware-field-file',
            itemId: 'firmware-field-file',
            required: false,
            allowBlank: true,
            afterBodyEl: [
                '<div class="x-form-display-field"><i>',
                Uni.I18n.translate('firmware.filesize.edit', 'FWC', 'The selected file will replace the already uploaded firmware file. Maximum file size is 150MB'),
                '</i></div>'
            ],
            anchor: '60%'
        },
        {
            xtype: 'textfield',
            itemId: 'text-image-identifier',
            name: 'imageIdentifier',
            fieldLabel: Uni.I18n.translate('general.imageIdentifier', 'FWC', 'Image identifier'),
            required: true,
            anchor: '60%'
        },
        {
            xtype: 'displayfield',
            itemId: 'disp-firmware-type',
            fieldLabel: Uni.I18n.translate('general.firmwareType', 'FWC', 'Firmware type'),
            name: 'type'
        },
        {
            xtype: 'displayfield',
            itemId: 'disp-firmware-status',
            fieldLabel: Uni.I18n.translate('firmware.field.status', 'FWC', 'Firmware status'),
            name: 'status'
        },
            {
                xtype: 'fieldcontainer',
                layout: 'hbox',
                flex: 1,
                itemId: 'firmware-min-meter-version-common',
                fieldLabel: Uni.I18n.translate('general.minMeterVersion', 'FWC', 'Minimal version meter firmware'),
                items: [
                        {
                            xtype: 'combobox',
                            itemId: 'firmware-min-meter-version',
                            allowBlank: false,
                            store: 'Fwc.store.MeterFirmwareDeps',
                            forceSelection: true,
                            queryMode: 'local',
                            displayField: 'name',
                            valueField: 'id',
                            hiddenName: 'meterFirmwareDependency',
                            listeners: {
                                beforerender: function () {
                                    var store = this.getStore();
                                    var deviceTypeId = this.up('firmware-form-edit').router.arguments.deviceTypeId;
                                    var proxy = store.getProxy();
                                    store.getProxy().setUrl(deviceTypeId);
                                    store.getProxy().setExtraParam('filter', Ext.encode([{ value: 'meter', property: 'firmwareType' }]));
                                    store.load();
                                },
                                change: function (combobox) {
                                    if (!this.resetButton) this.resetButton = this.nextSibling('#firmware-min-meter-version-default-button');
                                    if (combobox.getValue()) {
                                        this.resetButton.setDisabled(false);
                                    } else {
                                        this.resetButton.setDisabled(true);
                                    }
                                }
                            }
                        },
                        {
                            xtype: 'uni-default-button',
                            itemId: 'firmware-min-meter-version-default-button',
                            hidden: false,
                            disabled: true,
                            tooltip: Uni.I18n.translate('general.clear', 'UNI', 'Clear'),
                            margin: '0 30',
                            handler: function () {
                                this.previousSibling('#firmware-min-meter-version').reset();
                            }

                        }
                       ]
            },
            {
                xtype: 'fieldcontainer',
                layout: 'hbox',
                flex: 1,
                itemId: 'firmware-min-communication-version-common',
                fieldLabel: Uni.I18n.translate('general.minMeterVersion', 'FWC', 'Minimal version communication firmware'),
                items: [
                        {
                            xtype: 'combobox',
                            itemId: 'firmware-min-communication-version',
                            allowBlank: false,
                            store: 'Fwc.store.CommunicationFirmwareDeps',
                            forceSelection: true,
                            queryMode: 'local',
                            displayField: 'name',
                            valueField: 'id',
                            hiddenName: 'communicationFirmwareDependency',
                            listeners: {
                                beforerender: function () {
                                    var store = this.getStore();
                                    var deviceTypeId = this.up('firmware-form-edit').router.arguments.deviceTypeId;
                                    var proxy = store.getProxy();
                                    store.getProxy().setUrl(deviceTypeId);
                                    store.getProxy().setExtraParam('filter', Ext.encode([{ value: 'communication', property: 'firmwareType' }]));
                                    store.load();
                                },
                                change: function (combobox) {
                                    if (!this.resetButton) this.resetButton = this.nextSibling('#firmware-min-communication-version-default-button');
                                    if (combobox.getValue()) {
                                        this.resetButton.setDisabled(false);
                                    } else {
                                        this.resetButton.setDisabled(true);
                                    }
                                }
                            }
                        },
                        {
                            xtype: 'uni-default-button',
                            itemId: 'firmware-min-communication-version-default-button',
                            hidden: false,
                            disabled: true,
                            tooltip: Uni.I18n.translate('general.clear', 'UNI', 'Clear'),
                            margin: '0 30',
                            handler: function () {
                                this.previousSibling('#firmware-min-communication-version').reset();
                            }

                        }
                       ]
            },
        {
            xtype: 'hiddenfield',
            name: 'version'
        }
    ]
});