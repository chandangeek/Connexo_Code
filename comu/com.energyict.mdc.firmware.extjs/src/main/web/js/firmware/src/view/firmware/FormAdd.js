/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Fwc.view.firmware.FormAdd', {
    extend: 'Fwc.view.firmware.Form',
    xtype: 'firmware-form-add',
    edit: false,
    alias: 'widget.fwc-add-form',
    hydrator: 'Fwc.form.Hydrator',

    initComponent: function () {
        var me = this;
        router = me.router,
            deviceTypeId = me.deviceTypeId;


        me.items = [
            {
                xtype: 'uni-form-error-message',
                itemId: 'form-errors',
                margin: '0 0 10 0',
                anchor: '60%',
                hidden: true
            },
            {
                xtype: 'firmware-field-file',
                itemId: 'firmware-field-file',
                anchor: '60%'
            },
            {
                xtype: 'textfield',
                itemId: 'firmwareType',
                name: 'firmwareType',
                hidden: true
            },
            {
                xtype: 'textfield',
                itemId: 'firmwareStatus',
                name: 'firmwareStatus',
                hidden: true
            },
            {
                xtype: 'displayfield',
                itemId: 'disp-firmware-type',
                fieldLabel: Uni.I18n.translate('general.firmwareType', 'FWC', 'Firmware type'),
                name: 'type',
                hidden: true
            },
            {
                xtype: 'firmware-type',
                itemId: 'radio-firmware-type',
                defaultType: 'radiofield',
                value: {id: 'meter'},
                required: true,
                listeners: {
                    change: function (radio, newValue) {
                        if (newValue && Ext.isString(newValue.firmwareType)) {
                            if (newValue.firmwareType !== 'caConfigImage') {
                                if (me.supportedTypes && me.supportedTypes.length) {
                                    if (Ext.Array.filter(me.supportedTypes, function (item) {
                                        return item.data.id === "meter"
                                    }).length) {
                                        me.down('#firmware-min-meter-version-common').show();
                                    } else {
                                        me.down('#firmware-min-meter-version-common').hide();
                                    }
                                    if (Ext.Array.filter(me.supportedTypes, function (item) {
                                        return item.data.id === "communication"
                                    }).length) {
                                        me.down('#firmware-min-communication-version-common').show();
                                    } else {
                                        me.down('#firmware-min-communication-version-common').hide();
                                    }
                                    if (Ext.Array.filter(me.supportedTypes, function (item) {
                                        return item.data.id === "auxiliary"
                                    }).length) {
                                        me.down('#firmware-min-auxiliary-version-common').show();
                                    } else {
                                        me.down('#firmware-min-auxiliary-version-common').hide();
                                    }
                                } else {
                                    me.down('#firmware-min-meter-version-common').show();
                                    me.down('#firmware-min-communication-version-common').show();
                                    me.down('#firmware-min-auxiliary-version-common').show();
                                }
                                me.down('#text-imageIdentifier').show();
                                me.down('#text-firmware-version').setFieldLabel(
                                    Uni.I18n.translate('general.version', 'FWC', 'Version'));
                            } else {
                                me.down('#firmware-min-meter-version-common').hide();
                                me.down('#firmware-min-communication-version-common').hide();
                                me.down('#firmware-min-auxiliary-version-common').hide();
                                me.down('#text-imageIdentifier').hide();
                                me.down('#text-firmware-version').setFieldLabel(
                                    Uni.I18n.translate('general.versionImageIdentifier', 'FWC', 'Version/Image identifier'));
                            }
                        }
                    }
                }
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
                xtype: 'textfield',
                itemId: 'text-imageIdentifier',
                name: 'imageIdentifier',
                required: true,
                fieldLabel: Uni.I18n.translate('general.imageIdentifier', 'FWC', 'Image identifier'),
                anchor: '60%',
                allowBlank: false
            },
            {
                xtype: 'firmware-status',
                itemId: 'radio-firmware-status',
                defaultType: 'radiofield',
                value: {id: 'final'},
                required: true
            },
            {
                xtype: 'fieldcontainer',
                layout: 'hbox',
                flex: 1,
                itemId: 'firmware-min-meter-version-common',
                hidden: true,
                fieldLabel: Uni.I18n.translate('general.minMeterVersion', 'FWC', 'Minimum meter firmware version'),
                required: false,
                items: [
                    {
                        xtype: 'combobox',
                        itemId: 'firmware-min-meter-version',
                        allowBlank: false,
                        queryMode: 'local',
                        displayField: 'name',
                        valueField: 'id',
                        hiddenName: 'meterFirmwareDependency',
                        listeners: {
                            change: {
                                fn: function (combobox) {
                                    if (!this.resetButton) this.resetButton = this.nextSibling('#firmware-min-meter-version-default-button');
                                    if (combobox.getValue()) {
                                        this.resetButton.setDisabled(false);
                                    } else {
                                        this.resetButton.setDisabled(true);
                                    }
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
                hidden: true,
                fieldLabel: Uni.I18n.translate('general.minCommVersion', 'FWC', 'Minimum communication firmware version'),
                required: false,
                items: [
                    {
                        xtype: 'combobox',
                        itemId: 'firmware-min-communication-version',
                        allowBlank: false,
                        store: 'Fwc.store.CommunicationFirmwareDependencies',
                        displayField: 'name',
                        valueField: 'id',
                        hiddenName: 'communicationFirmwareDependency',
                        queryMode: 'local',
                        listeners: {
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
                hidden: true,
                fieldLabel: Uni.I18n.translate('general.minAuxiliaryVersion', 'FWC', 'Minimum auxiliary firmware version'),
                required: false,
                items: [
                    {
                        xtype: 'combobox',
                        itemId: 'firmware-min-auxiliary-version',
                        allowBlank: false,
                        store: 'Fwc.store.AuxiliaryFirmwareDependencies',
                        displayField: 'name',
                        valueField: 'id',
                        hiddenName: 'auxiliaryFirmwareDependency',
                        queryMode: 'local',
                        listeners: {
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
            }
        ];

        function setMinLevelComboBoxStore(cbxId, storeName, storeType) {
            var store = Ext.data.StoreManager.lookup(storeName);
            var proxy = store.getProxy();
            proxy.setUrl(deviceTypeId);
            proxy.setExtraParam('filter', Ext.encode([{value: storeType, property: 'firmwareType'}]));
            store.load(function (records) {
                var combobox = me.down('#' + cbxId);
                if (!Ext.isEmpty(records)) {
                    combobox.bindStore(store);
                } else {
                    combobox.hide();
                    combobox.nextSibling('uni-default-button').hide();
                    combobox.up('fieldcontainer').add({
                        itemId: cbxId + '-common-dispfld',
                        xtype: 'displayfield',
                        value: Uni.I18n.translate('general.noFirmFiles', 'FWC', 'There are no firmware files of this type uploaded to the device type'),
                        fieldStyle: 'color: red'
                    });
                }
            });
        }

        setMinLevelComboBoxStore('firmware-min-meter-version', 'Fwc.store.MeterFirmwareDependencies', 'meter');
        setMinLevelComboBoxStore('firmware-min-communication-version', 'Fwc.store.CommunicationFirmwareDependencies', 'communication');
        setMinLevelComboBoxStore('firmware-min-auxiliary-version', 'Fwc.store.AuxiliaryFirmwareDependencies', 'auxiliary');

        me.callParent(arguments);
    },
    updateGrid: function () {
        var me = this,
            grid = me.down('#reading-types-grid'),
            emptyLabel = me.down('#noReadingTypesForEstimationRuleLabel');
        if (grid.getStore().count() === 0) {
            emptyLabel.show();
            grid.hide();
        } else {
            emptyLabel.hide();
            grid.show();
        }
    }
});