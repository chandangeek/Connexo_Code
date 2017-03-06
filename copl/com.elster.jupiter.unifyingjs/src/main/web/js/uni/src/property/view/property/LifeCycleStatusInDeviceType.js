/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */


Ext.define('Uni.property.view.property.LifeCycleStatusInDeviceType', {
    extend: 'Uni.property.view.property.Base',

    selType: 'checkboxmodel',

    getEditCmp: function () {
        var me = this;
        me.layout = 'vbox';
        return [
            {
                items: [
                    {
                        xtype: 'uni-grid-filtertop-combobox',
                        itemId: 'device-type',
                        fieldLabel: Uni.I18n.translate('device.state', 'UNI', 'Device state'),
                        queryMode: 'local',
                        name: 'deviceType',
                        labelWidth: 260,
                        width: 595,
                        emptyText: Uni.I18n.translate('general.device.type.empty', 'UNI', 'Select a device type...'),
                        valueField: 'id',
                        displayField: 'deviceTypeName',
                        allowBlank: false,
                        multiSelect: true,
                        store: me.getDeviceTypeStore(),
                        msgTarget: 'under',
                        required: me.property.get('required'),
                        listeners: {
                            change: function (combo, newValue, oldValue) {
                                var deviceStateCombo = me.down('#device-state');
                                deviceStateCombo.getStore().clearFilter();
                                deviceStateCombo.getStore().filter([{
                                    filterFn: function(item) {
                                        return newValue.indexOf(item.get('deviceId')) !=-1;
                                    }
                                }]);
                                deviceStateCombo.setValue(deviceStateCombo.getValue().filter(function(value){
                                    return newValue.indexOf(value.split(":")[0]) != -1;
                                }));

                                deviceStateCombo.setDisabled(!deviceStateCombo.getStore().count() > 0);
                            }
                        }
                    },
                    {
                        xtype: 'uni-grid-filtertop-combobox',
                        itemId: 'device-state',
                        enabled: false,
                        disabled: true,
                        fieldLabel: '&nbsp',
                        queryMode: 'local',
                        name: this.getName(),
                        labelWidth: 260,
                        allowBlank: false,
                        width: 595,
                        valueField: 'id',
                        displayField: 'lifeCycleStateName',
                        store: me.getDeviceStateStore(),
                        editable: false,
                        multiSelect: true,
                        msgTarget: 'under',
                        emptyText: Uni.I18n.translate('general.device.state.empty', 'UNI', 'Select a device state...'),
                    }
                ]
            }
        ];
    },

    setLocalizedName: function (name) {
    },

    setValue: function (value) {
        var me = this,
            deviceTypeValues = [],
            deviceStateValues = [],
            deviceTypeCombo = me.down('#device-type'),
            deviceStateCombo = me.down('#device-state');

        value = [].concat( value );
        Ext.Array.forEach(value, function (item) {
            if(item !== '') {
                deviceTypeValues.push(item.split(':')[0]);
                deviceStateValues.push(item);
            }
        });
        var uniqueDeviceTypeValues = deviceTypeValues.filter(function(item, pos, self) {
            return self.indexOf(item) == pos;
        })
        deviceTypeCombo.setValue(uniqueDeviceTypeValues);
        deviceStateCombo.setValue(deviceStateValues);
    },

    getValue: function () {
        var me = this,
            deviceStateCombo = me.down('#device-state');

        return deviceStateCombo.getValue()
    },

    getDeviceTypeStore: function () {
        var me = this,
            possibleValues = me.getProperty().getPossibleValues();

        var elements = [];
        if(possibleValues){
            for (var i= 0 ; i< possibleValues.length; i++){
                var object = {};
                object.id = possibleValues[i].id.split(":")[0];
                object.deviceTypeName = JSON.parse(possibleValues[i].name).deviceTypeName;
                elements.push(object);
            }
        }
        return Ext.create('Ext.data.JsonStore', {
            fields: [
                {
                    name: 'id'
                },
                {
                    name: 'deviceTypeName'
                }
            ],
            data: elements
        });

    },

    getDeviceStateStore: function () {
        var me = this,
            possibleValues = me.getProperty().getPossibleValues();

        var elements = [];
        if(possibleValues){
            for (var i= 0 ; i< possibleValues.length; i++){
                var object = {};
                object.id = possibleValues[i].id;
                object.deviceId = possibleValues[i].id.split(":")[0];
                object.lifeCycleStateName = JSON.parse(possibleValues[i].name).lifeCycleStateName;
                object.deviceTypeName = JSON.parse(possibleValues[i].name).deviceTypeName;
                elements.push(object);
            }
        }
        return Ext.create('Ext.data.JsonStore', {
            fields: [
                {
                    name: 'id'
                },
                {
                    name: 'deviceId'
                },
                {
                    name: 'lifeCycleStateName'
                },
                {
                    name: 'deviceTypeName'
                }
            ],
            data: elements,
            filters: [
                function (item) {
                    return item.id < -1;
                }
            ]
        });

    }

});