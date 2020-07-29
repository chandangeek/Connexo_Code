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
            text: 'This version is in use and only the image identifier and minimum FW versions can be modified',
            value: 'This version is in use and only the image identifier and minimum FW versions can be modified',
			hidden: true,
			width: 600
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
            name: 'type',
            renderer: function (value){
                var form = this.up('firmware-form-edit');
                if (form){
                    if (value === 'Image') {
                        form.down('#firmware-min-meter-version-common').hide();
                        form.down('#firmware-min-communication-version').hide();
                    }
                    else {
                        form.down('#firmware-min-meter-version-common').show();
                        form.down('#firmware-min-communication-version').show();
                    }
                }
                return value;
            }
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
            fieldLabel: Uni.I18n.translate('general.minMeterVersion', 'FWC', 'Minimum meter firmware version'),
            items: [
                       {    xtype: 'combobox',
                            itemId: 'firmware-min-meter-version',
                            allowBlank: false,
                            store: 'Fwc.store.MeterFirmwareDependenciesEdit',
                            forceSelection: true,
                            queryMode: 'local',
                            displayField: 'name',
                            valueField: 'id',
                            hiddenName: 'meterFirmwareDependency',
                            listeners: {
                               beforerender: function () {
                                    var store = this.getStore();
                                    var me = this;
                                    var formRecord = this.up('firmware-form-edit').getRecord();
                                    var versionId = formRecord.getId();
                                    var currMeterId;
                                    var curMeterVersion = formRecord.getMeterFirmwareDependency();
                                    if (curMeterVersion){
                                        currMeterId = curMeterVersion.getId();
                                    }
                                    var proxy = store.getProxy();
                                    store.getProxy().setUrl(versionId);
                                    store.getProxy().setExtraParam('filter', Ext.encode([{ value: 'meter', property: 'firmwareType' }]));
                                    var combobox = this;
                                    store.load(function (data) {
                                        if ( !data || !data.length){
                                            combobox.hide();
                                            combobox.nextSibling('uni-default-button').hide();
                                            combobox.up('fieldcontainer').add({
                                                  itemId: 'firmware-min-meter-version-common-dispfld',
                                                  xtype: 'displayfield',
                                                  value: Uni.I18n.translate('general.noFirmFiles', 'FWC', 'There are no firmware files of this type uploaded to the device type'),
                                                  fieldStyle: 'color: red'
                                            });
                                            return;
                                        }
                                        for (var iCnt = 0; iCnt < data.length; iCnt++){
                                            var item = data[iCnt];
                                            if (item.getId() === currMeterId) me.setValue(item);
                                        }
                                    });
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
                            tooltip: Uni.I18n.translate('general.clear', 'FWC', 'Clear'),
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
                fieldLabel: Uni.I18n.translate('general.minCommVersion', 'FWC', 'Minimum communication firmware version'),
                items: [
                        {
                            xtype: 'combobox',
                            itemId: 'firmware-min-communication-version',
                            allowBlank: false,
                            store: 'Fwc.store.CommunicationFirmwareDependenciesEdit',
                            forceSelection: true,
                            queryMode: 'local',
                            displayField: 'name',
                            valueField: 'id',
                            hiddenName: 'communicationFirmwareDependency',
                            listeners: {
                                beforerender: function () {
                                    var me = this;
                                    var store = this.getStore();
                                    var formRecord = this.up('firmware-form-edit').getRecord();
                                    var versionId = formRecord.getId();
                                    var currCommunicationId;
                                    var curCommunicationVersion = formRecord.getCommunicationFirmwareDependency();
                                    var combobox = this;
                                    if (curCommunicationVersion){
                                        currCommunicationId = curCommunicationVersion.getId();
                                    }
                                    var proxy = store.getProxy();
                                    store.getProxy().setUrl(versionId);
                                    store.getProxy().setExtraParam('filter', Ext.encode([{ value: 'communication', property: 'firmwareType' }]));
                                    store.load(function (data) {
                                        if ( !data || !data.length){
                                            combobox.hide();
                                            combobox.nextSibling('uni-default-button').hide();
                                            combobox.up('fieldcontainer').add({
                                                  itemId: 'firmware-min-communication-version-common-dispfld',
                                                  xtype: 'displayfield',
                                                  value: Uni.I18n.translate('general.noFirmFiles', 'FWC', 'There are no firmware files of this type uploaded to the device type'),
                                                  fieldStyle: 'color: red'
                                            });
                                            return;
                                        }
                                        for (var iCnt = 0; iCnt < data.length; iCnt++){
                                            var item = data[iCnt];
                                            if (item.getId() === currCommunicationId) me.setValue(item);
                                        }
                                    });
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
                            tooltip: Uni.I18n.translate('general.clear', 'FWC', 'Clear'),
                            margin: '0 30',
                            handler: function () {
                                this.previousSibling('#firmware-min-communication-version').reset();
                            }

                        }
                       ]
            },
        {
            xtype: 'fieldcontainer',
            layout: 'hbox',
            flex: 1,
            itemId: 'firmware-min-auxiliary-version-common',
            fieldLabel: Uni.I18n.translate('general.minAuxiliaryVersion', 'FWC', 'Minimum auxiliary firmware version'),
            items: [
                       {    xtype: 'combobox',
                            itemId: 'firmware-min-auxiliary-version',
                            allowBlank: false,
                            store: 'Fwc.store.AuxiliaryFirmwareDependenciesEdit',
                            forceSelection: true,
                            queryMode: 'local',
                            displayField: 'name',
                            valueField: 'id',
                            hiddenName: 'auxiliaryFirmwareDependency',
                            listeners: {
                               beforerender: function () {
                                    var store = this.getStore();
                                    var me = this;
                                    var formRecord = this.up('firmware-form-edit').getRecord();
                                    var versionId = formRecord.getId();
                                    var currAuxiliaryId;
                                    var curAuxiliaryVersion = formRecord.getAuxiliaryFirmwareDependency();
                                    if (curAuxiliaryVersion){
                                        currAuxiliaryId = curAuxiliaryVersion.getId();
                                    }
                                    var proxy = store.getProxy();
                                    store.getProxy().setUrl(versionId);
                                    store.getProxy().setExtraParam('filter', Ext.encode([{ value: 'auxiliary', property: 'firmwareType' }]));
                                    var combobox = this;
                                    store.load(function (data) {
                                        if ( !data || !data.length){
                                            combobox.hide();
                                            combobox.nextSibling('uni-default-button').hide();
                                            combobox.up('fieldcontainer').add({
                                                  itemId: 'firmware-min-auxiliary-version-common-dispfld',
                                                  xtype: 'displayfield',
                                                  value: Uni.I18n.translate('general.noFirmFiles', 'FWC', 'There are no firmware files of this type uploaded to the device type'),
                                                  fieldStyle: 'color: red'
                                            });
                                            return;
                                        }
                                        for (var iCnt = 0; iCnt < data.length; iCnt++){
                                            var item = data[iCnt];
                                            if (item.getId() === currAuxiliaryId) me.setValue(item);
                                        }
                                    });
                                },
                                change: function (combobox) {
                                    if (!this.resetButton) this.resetButton = this.nextSibling('#firmware-min-auxiliary-version-default-button');
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
                            itemId: 'firmware-min-auxiliary-version-default-button',
                            hidden: false,
                            disabled: true,
                            tooltip: Uni.I18n.translate('general.clear', 'FWC', 'Clear'),
                            margin: '0 30',
                            handler: function () {
                                this.previousSibling('#firmware-min-auxiliary-version').reset();
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