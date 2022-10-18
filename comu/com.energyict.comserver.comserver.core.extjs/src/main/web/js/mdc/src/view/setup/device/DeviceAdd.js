/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.device.DeviceAdd', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceAdd',
    itemId: 'deviceAdd',
    requires: [
        'Mdc.store.AvailableDeviceTypes',
        'Mdc.store.AvailableDeviceConfigurations',
        'Uni.util.Hydrator',
        'Mdc.widget.DeviceConfigurationField'
    ],
    labelWidth: 250,
    formWidth: 1000,
    deviceTypeStore: undefined,

    initComponent: function () {
        var me = this;
        me.content = [
            {
                xtype: 'panel',
                ui: 'large',
                layout: 'vbox',
                title: Uni.I18n.translate('deviceAdd.title', 'MDC', 'Add device'),
                itemId: 'deviceAddPanel',

                items: [
                    {
                        xtype: 'form',
                        width: me.formWidth,
                        itemId: 'editForm',
                        hydrator: 'Uni.util.Hydrator',
                        buttonAlign: 'left',
                        layout: {
                            type: 'vbox',
                            align: 'left'
                        },
                        defaults: {
                            labelWidth: me.labelWidth,
                            width: 750
                        },
                        items: [
                            {
                                name: 'errors',
                                ui: 'form-error-framed',
                                itemId: 'addDeviceFormErrors',
                                layout: 'hbox',
                                margin: '0 0 10 0',
                                hidden: true,
                                defaults: {
                                    xtype: 'container',
                                    margin: '0 0 0 10'
                                }
                            },
                            {
                                xtype: 'textfield',
                                name: 'name',
                                itemId: 'deviceAddName',
                                fieldLabel: Uni.I18n.translate('deviceAdd.name', 'MDC', 'Name'),
                                required: true,
                                allowBlank: false,
                                msgTarget: 'under',
                                maxLength: 80,
                                enforceMaxLength: true,
                                listeners: {
                                    afterrender: function (field) {
                                        field.focus(false, 200);
                                    }
                                },
                                vtype: 'checkForBlacklistCharacters'
                            },
                            {
                                xtype: 'textfield',
                                name: 'serialNumber',
                                itemId: 'deviceAddSerial',
                                fieldLabel: Uni.I18n.translate('deviceAdd.serialNumber', 'MDC', 'Serial number'),
                                maxLength: 80,
                                vtype: 'checkForBlacklistCharacters',
                                enforceMaxLength: true
                            },
                            {
                                xtype: 'deviceConfigurationField',
                                itemId: 'deviceConfiguration',
                                deviceTypeStore: 'AvailableDeviceTypes',
                                queryMode: 'local',
                                allowBlank: false
                            },
                        {
                            xtype: 'textfield',
                            name: 'manufacturer',
                            itemId: 'deviceAddManufacturer',
                            fieldLabel: Uni.I18n.translate('deviceAdd.manufacturer', 'MDC', 'Manufacturer'),
                            maxLength: 80,
                            vtype: 'checkForBlacklistCharacters',
                            enforceMaxLength: true
                        },
                            {
                                xtype: 'textfield',
                                name: 'modelNbr',
                                itemId: 'deviceAddModelNumber',
                                fieldLabel: Uni.I18n.translate('deviceAdd.modelNumber', 'MDC', 'Model number'),
                                maxLength: 80,
                                vtype: 'checkForBlacklistCharacters',
                                enforceMaxLength: true
                            },
                            {
                                xtype: 'textfield',
                                name: 'modelVersion',
                                itemId: 'deviceAddModelVersion',
                                fieldLabel: Uni.I18n.translate('deviceAdd.modelVersion', 'MDC', 'Model version'),
                                maxLength: 80,
                                vtype: 'checkForBlacklistCharacters',
                                enforceMaxLength: true
                            },
                            {
                            xtype: 'datefield',
                            itemId: 'deviceAddShipmentDate',
                            allowBlank: false,
                            required: true,
                            maxWidth: 400,
                            editable: false,
                            value: new Date(new Date().setHours(0,0,0,0)), // today midnight
                            fieldLabel: Uni.I18n.translate('deviceAdd.shipmentDate', 'MDC', 'Shipment date'),
                            format: Uni.util.Preferences.lookup(Uni.DateTime.dateShortKey, Uni.DateTime.dateShortDefault)
                        },
                        {
                            xtype: 'combobox',
                            maxWidth: 400,
                            name: 'yearOfCertification',
                            itemId: 'deviceAddCertification',
                            fieldLabel: Uni.I18n.translate('deviceAdd.yearOfCertification', 'MDC', 'Year of certification'),
                            displayField: 'year',
                            valueField: 'year',
                            store: undefined,
                            editable: false,
                            listConfig: { maxHeight: 100 },
                            listeners: {
                                beforerender: function (combo) {
                                    var currentTime = new Date();
                                    var year = currentTime.getFullYear();
                                    var years = [];

                                        for(y=0; y<=20; y++){
                                            years.push([year-y]);
                                        }

                                        var yearStore = new Ext.data.SimpleStore
                                        ({
                                            fields : ['year'],
                                            data : years
                                        });

                                        combo.bindStore(yearStore);
                                        combo.setValue(year);
                                    }
                                }
                            },
                            {
                                xtype: 'textfield',
                                name: 'batch',
                                itemId: 'deviceAddBatch',
                                fieldLabel: Uni.I18n.translate('deviceAdd.batch', 'MDC', 'Batch'),
                                maxLength: 80,
                                vtype: 'checkForBlacklistCharacters',
                                enforceMaxLength: true
                            },
                            {
                                xtype: 'fieldcontainer',
                                itemId: 'mdc-deviceAdd-btnContainer',
                                ui: 'actions',
                                fieldLabel: '&nbsp',
                                layout: {
                                    type: 'hbox',
                                    align: 'stretch'
                                },
                                items: [
                                    {
                                        text: Uni.I18n.translate('general.add', 'MDC', 'Add'),
                                        xtype: 'button',
                                        ui: 'action',
                                        action: 'save',
                                        itemId: 'deviceAddSaveButton'
                                    },
                                    {
                                        text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel'),
                                        xtype: 'button',
                                        ui: 'link',
                                        itemId: 'cancelLink',
                                        href: '#/devices/'
                                    }
                                ]
                            }
                        ]
                    }
                ]
            }
        ];
        me.callParent(arguments);
    }

});
