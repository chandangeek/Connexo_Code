/**
 * Created by pdo on 27/03/2017.
 */
Ext.define('Mdc.view.setup.dataloggerslaves.DataLoggerSlaveDeviceAdd', {
    extend: 'Ext.form.Panel',
    alias: 'widget.datalogger-slave-device-add',
    itemId: 'mdc-datalogger-slave-device-add',
    requires: [
        'Mdc.util.SlaveNameProposal',
        'Mdc.widget.DeviceConfigurationField',
        'Mdc.store.AvailableDeviceTypes'
    ],
    hydrator: 'Uni.util.Hydrator',
    width: 570,
    margin: '20 0 0 0',
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    defaults: {
        labelWidth: 145
    },
    deviceTypeStore: undefined,
    dataLogger: null,
    initComponent: function () {
        var me = this;
        me.items = [
            {   xtype: 'deviceConfigurationField',
                itemId: 'dataLoggerSlaveDeviceConfiguration',
                deviceTypeStore: me.deviceTypeStore,
                queryMode: 'local',
                allowBlank: false,
                extraConfigListener : [{listener: me.setLinkPurpose, scope: me}]
            },
            {
                xtype: 'textfield',
                name: 'name',
                itemId: 'dataLoggerSlaveDeviceName',
                fieldLabel: Uni.I18n.translate('deviceAdd.name', 'MDC', 'Name'),
                required: true,
                msgTarget: 'under',
                maxLength: 80,
                enforceMaxLength: true,
                validateOnBlur: false,
                validateOnChange: false,
                vtype: 'checkForBlacklistCharacters'
            },
            {
                xtype:'container',
                itemId: 'dataLoggerSlaveFields',
                layout: {
                    type: 'vbox',
                    align: 'stretch',
                },
                hidden: true,
                defaults: me.defaults,
                items: [
                    {
                        xtype: 'textfield',
                        name: 'serialNumber',
                        itemId: 'dataLoggerSlaveSerial',
                        fieldLabel: Uni.I18n.translate('deviceAdd.serialNumber', 'MDC', 'Serial number'),
                        maxLength: 80,
                        enforceMaxLength: true,
                        vtype: 'checkForBlacklistCharacters'
                    },
                    {
                        xtype: 'textfield',
                        name: 'manufacturer',
                        itemId: 'dataLoggerSlaveManufacturer',
                        fieldLabel: Uni.I18n.translate('deviceAdd.manufacturer', 'MDC', 'Manufacturer'),
                        maxLength: 80,
                        enforceMaxLength: true,
                        vtype: 'checkForBlacklistCharacters'
                    },
                    {
                        xtype: 'textfield',
                        name: 'modelNbr',
                        itemId: 'dataLoggerSlaveModelNumber',
                        fieldLabel: Uni.I18n.translate('deviceAdd.modelNumber', 'MDC', 'Model number'),
                        maxLength: 80,
                        enforceMaxLength: true,
                        vtype: 'checkForBlacklistCharacters'
                    },
                    {
                        xtype: 'textfield',
                        name: 'modelVersion',
                        itemId: 'dataLoggerSlaveModelVersion',
                        fieldLabel: Uni.I18n.translate('deviceAdd.modelVersion', 'MDC', 'Model version'),
                        maxLength: 80,
                        enforceMaxLength: true,
                        vtype: 'checkForBlacklistCharacters'
                    },
                     {
                         xtype: 'datefield',
                         itemId: 'dataLoggerSlaveShipmentDate',
                         allowBlank: false,
                         required: true,
                         maxWidth: 400,
                         editable: false,
                         value: new Date(new Date().setHours(0, 0, 0, 0)), // today midnight
                         fieldLabel: Uni.I18n.translate('deviceAdd.shipmentDate', 'MDC', 'Shipment date'),
                         format: Uni.util.Preferences.lookup(Uni.DateTime.dateShortKey, Uni.DateTime.dateShortDefault)
                     },
                     {
                         xtype: 'combobox',
                         maxWidth: 400,
                         name: 'yearOfCertification',
                         itemId: 'dataLoggerSlaveCertification',
                         fieldLabel: Uni.I18n.translate('deviceAdd.yearOfCertification', 'MDC', 'Year of certification'),
                         displayField: 'year',
                         valueField: 'year',
                         store: undefined,
                         editable: false,
                         listConfig: {maxHeight: 100},
                         listeners: {
                             beforerender: function (combo) {
                                 var currentTime = new Date();
                                 var year = currentTime.getFullYear();
                                 var years = [];

                                 for (y = 0; y <= 20; y++) {
                                     years.push([year - y]);
                                 }

                                 var yearStore = new Ext.data.SimpleStore
                                 ({
                                     fields: ['year'],
                                     data: years
                                 });

                                 combo.bindStore(yearStore);
                                 combo.setValue(year);
                             }
                         }
                     },
                    {
                        xtype: 'textfield',
                        name: 'batch',
                        itemId: 'dataLoggerSlaveBatch',
                        fieldLabel: Uni.I18n.translate('deviceAdd.batch', 'MDC', 'Batch'),
                        maxLength: 80,
                        enforceMaxLength: true,
                        vtype: 'checkForBlacklistCharacters'
                    }

                ]
            }
         ];
        me.callParent(arguments);
    },
    setLinkPurpose: function() {
        var me = this,
            deviceConfigurationField = me.down('#dataLoggerSlaveDeviceConfiguration'),
            deviceType = deviceConfigurationField.getDeviceType(),
            deviceConfiguration = deviceConfigurationField.getDeviceConfiguration(),
            dataLoggerSlaveFields = me.down('#dataLoggerSlaveFields'),
            nameTextField = me.down('#dataLoggerSlaveDeviceName');
        if (deviceType.isDataLoggerSlave()){
            dataLoggerSlaveFields.show();
        }else{
            dataLoggerSlaveFields.hide();
            if (me.dataLogger && nameTextField && !nameTextField.getValue()) {
                nameTextField.setValue(Mdc.util.SlaveNameProposal.get(me.dataLogger, deviceConfiguration));
            }
        }
    }
});
