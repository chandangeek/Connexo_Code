/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Ddv.view.Preview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.ddv-quality-preview',
    router: null,
    title: ' ',

    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'form',
                itemId: 'ddv-quality-form',
                layout: 'column',
                defaults: {
                    xtype: 'container',
                    columnWidth: 0.5
                },
                items: [
                    {
                        defaults: {
                            xtype: 'fieldcontainer'
                        },
                        items: [
                            {
                                itemId: 'device-info-container',
                                defaults: {
                                    xtype: 'displayfield',
                                    labelWidth: 250
                                },
                                items: [
                                    {
                                        fieldLabel: Uni.I18n.translate('general.name', 'DDV', 'Name'),
                                        name: 'deviceName',
                                        itemId: 'device-name-field',
                                        renderer: function (value) {
                                            if (value) {
                                                var url = me.router.getRoute('devices/device').buildUrl({
                                                    deviceId: encodeURIComponent(value)
                                                });
                                                return Mdc.privileges.Device.canViewDeviceCommunication() ? '<a href="' + url + '">' + Ext.String.htmlEncode(value) + '</a>' : value;
                                            }
                                        }
                                    },
                                    {
                                        fieldLabel: Uni.I18n.translate('general.serialNumber', 'DDV', 'Serial number'),
                                        name: 'deviceSerialNumber',
                                        itemId: 'serial-number-validations-preview'
                                    },
                                    {
                                        fieldLabel: Uni.I18n.translate('general.deviceType', 'DDV', 'Device type'),
                                        name: 'deviceType',
                                        itemId: 'device-type-field',
                                        renderer: function (value) {
                                            if (value) {
                                                var url = me.router.getRoute('administration/devicetypes/view').buildUrl({
                                                    deviceTypeId: value.id
                                                });
                                                return Mdc.privileges.DeviceType.canView() ? '<a href="' + url + '">' + Ext.String.htmlEncode(value.name) + '</a>' : Ext.String.htmlEncode(value.name);
                                            }

                                        }
                                    },
                                    {
                                        fieldLabel: Uni.I18n.translate('general.configuration', 'DDV', 'Configuration'),
                                        name: 'deviceConfig',
                                        itemId: 'device-config-field',
                                        renderer: function (value) {
                                            var record = me.down('form').getRecord();

                                            if (record) {
                                                var url = me.router.getRoute('administration/devicetypes/view/deviceconfigurations/view').buildUrl({
                                                    deviceTypeId: record.get('deviceType').id,
                                                    deviceConfigurationId: value.id
                                                });
                                                return Mdc.privileges.DeviceType.canView() ? '<a href="' + url + '">' + Ext.String.htmlEncode(value.name) + '</a>' : Ext.String.htmlEncode(value.name);
                                            }
                                        }
                                    }
                                ]
                            },
                            {
                                fieldLabel: Uni.I18n.translate('general.suspectReadings', 'DDV', 'Suspect readings'),
                                itemId: 'suspect-readings-container',
                                labelAlign: 'top',
                                defaults: {
                                    xtype: 'displayfield',
                                    labelWidth: 250
                                },
                                items: [
                                    {
                                        fieldLabel: Uni.I18n.translate('general.registers', 'DDV', 'Registers'),
                                        name: 'registerSuspects',
                                        itemId: 'registers-field'
                                    },
                                    {
                                        fieldLabel: Uni.I18n.translate('general.channels', 'DDV', 'Channels'),
                                        name: 'channelSuspects',
                                        itemId: 'channels-field'
                                    },
                                    {
                                        fieldLabel: Uni.I18n.translate('general.lastSuspect', 'DDV', 'Last suspect'),
                                        name: 'lastSuspect',
                                        itemId: 'last-suspect-field',
                                        renderer: function (value) {
                                            return value ? Uni.DateTime.formatDateLong(new Date(value)) : '-';
                                        }
                                    }
                                ]
                            },
                            {
                                fieldLabel: Uni.I18n.translate('general.typeOfSuspects', 'DDV', 'Type of suspects'),
                                itemId: 'type-of-suspects-container',
                                labelAlign: 'top',
                                defaults: {
                                    xtype: 'displayfield',
                                    labelWidth: 250
                                }
                            }
                        ]
                    },
                    {
                        defaults: {
                            xtype: 'fieldcontainer'
                        },
                        items: [
                            {
                                margin: '0 0 0 150',
                                fieldLabel: Uni.I18n.translate('general.dataQuality', 'DDV', 'Data quality'),
                                itemId: 'data-quality-container',
                                defaults: {
                                    xtype: 'displayfield',
                                    labelWidth: 250,
                                    fieldLabel: ''
                                },
                                items: [
                                    {
                                        name: 'amountOfSuspects',
                                        itemId: 'amount-of-suspects',
                                        renderer: function (value) {
                                            return '<span class="icon-flag5" style="color:red; margin-right:10px" data-qtip="'
                                                + Uni.I18n.translate('general.suspects', 'DDV', 'Suspects') + '"></span>' + value;
                                        }
                                    },
                                    {
                                        name: 'amountOfConfirmed',
                                        itemId: 'amount-of-confirmed',
                                        renderer: function (value) {
                                            return '<span class="icon-checkmark" style="margin-right:10px" data-qtip="'
                                                + Uni.I18n.translate('general.confirmed', 'DDV', 'Confirmed') + '"></span>' + value;
                                        }
                                    },
                                    {
                                        name: 'amountOfEstimates',
                                        itemId: 'amount-of-estimates',
                                        renderer: function (value) {
                                            return '<span class="icon-flag5" style="color:#33CC33; margin-right:10px" data-qtip="'
                                                + Uni.I18n.translate('general.estimates', 'DDV', 'Estimates') + '"></span>' + value;
                                        }
                                    },
                                    {
                                        name: 'amountOfInformatives',
                                        itemId: 'amount-of-informatives',
                                        renderer: function (value) {
                                            return '<span class="icon-flag5" style="color:#dedc49; margin-right:10px" data-qtip="'
                                                + Uni.I18n.translate('general.informatives', 'DDV', 'Informatives') + '"></span>' + value;
                                        }
                                    }
                                ]
                            },
                            {
                                fieldLabel: Uni.I18n.translate('general.editedValues', 'DDV', 'Edited values'),
                                itemId: 'edited-values-container',
                                labelAlign: 'top',
                                defaults: {
                                    xtype: 'displayfield',
                                    labelWidth: 250
                                },
                                items: [
                                    {
                                        fieldLabel: Uni.I18n.translate('general.added', 'DDV', 'Added'),
                                        name: 'amountOfAdded',
                                        itemId: 'amount-of-added'
                                    },
                                    {
                                        fieldLabel: Uni.I18n.translate('general.edited', 'DDV', 'Edited'),
                                        name: 'amountOfEdited',
                                        itemId: 'amount-of-edited'
                                    },
                                    {
                                        fieldLabel: Uni.I18n.translate('general.removed', 'DDV', 'Removed'),
                                        name: 'amountOfRemoved',
                                        itemId: 'amount-of-removed'
                                    }
                                ]
                            },
                            {
                                fieldLabel: Uni.I18n.translate('general.typeOfEstimates', 'DDV', 'Type of estimates'),
                                itemId: 'type-of-estimates-container',
                                labelAlign: 'top',
                                defaults: {
                                    xtype: 'displayfield',
                                    labelWidth: 250
                                }
                            }
                        ]
                    }
                ]
            }
        ];

        me.callParent();
    },

    loadRecord: function (record) {
        var me = this;

        Ext.suspendLayouts();
        me.setTitle(record.get('deviceName'));
        me.down('#ddv-quality-form').loadRecord(record);
        updateContainer(me.down('#type-of-suspects-container'), record.suspectsPerValidator().getRange().map(prepareItem));
        updateContainer(me.down('#type-of-estimates-container'), record.estimatesPerEstimator().getRange().map(prepareItem));
        Ext.resumeLayouts(true);

        function updateContainer(container, items) {
            container.removeAll();
            container.add(items);
        }

        function prepareItem(item) {
            return {
                itemId: item.get('name'),
                fieldLabel: item.get('name'),
                value: item.get('value')
            }
        }
    }
});