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
                        editable: false,
                        required: me.property.get('required'),
                        listeners: {
                            change: function (combo, newValue, oldValue) {
                                var deviceStateCombo = me.down('#device-state');
                                var store = me.getDeviceStateStore();
                                var elements = [];
                                store.each(function(record) {
                                    if (newValue.indexOf(record.get('deviceId')) == -1){
                                        return;
                                    }

                                    var result = elements.filter(function( element) {
                                        return element.lifeCycleStateName == record.get('lifeCycleStateName');
                                    });

                                    if (result.length == 0){
                                        var object = {};
                                        object.deviceId = record.get('deviceId');
                                        object.lifeCycleId = record.get('lifeCycleId');
                                        object.deviceTypeName = record.get('deviceTypeName');
                                        object.id = record.get('id');
                                        object.lifeCycleStateName = record.get('lifeCycleStateName');
                                        elements.push(object);
                                    }
                                });

                                var filteredStore = Ext.create('Ext.data.JsonStore', {
                                    fields: ['id', 'deviceId', 'lifeCycleStateName', 'deviceTypeName', 'lifeCycleId'],
                                    sorters: [{
                                        property: 'lifeCycleStateName',
                                        direction: 'ASC'
                                    }],
                                    groupField: 'lifeCycleId',
                                    data: elements
                                });
                                deviceStateCombo.queryFilter = null;
                                if (elements.length == 0){
                                    deviceStateCombo.clearValue();
                                    deviceStateCombo.bindStore(null);
                                    deviceStateCombo.setDisabled(true);

                                } else {
                                    deviceStateCombo.bindStore(filteredStore);
                                    if (deviceStateCombo.getValue().length>0)
                                        deviceStateCombo.setValue(deviceStateCombo.getValue().filter(function(value){
                                            return newValue.indexOf(value.split(":")[0]) != -1;
                                        }));
                                    deviceStateCombo.setDisabled(false);
                                }
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
                        emptyText: Uni.I18n.translate('general.device.state.empty', 'UNI', 'Select a device state...')
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

        if(value) {
            value = [].concat(value);
            Ext.Array.forEach(value, function (item) {
                if (item !== '') {
                    deviceTypeValues.push(item.split(':')[0]);
                    if(item.split(':')[2].split(',').length > 0){
                        for(i = 0; i < item.split(':')[2].split(',').length; i++){
                            deviceStateValues.push(item.split(':')[0] + ':' + item.split(':')[1] + ':' + item.split(':')[2].split(',')[i]);
                        }
                    } else{
                        deviceStateValues.push(item);
                    }
                }
            });
            var uniqueDeviceTypeValues = deviceTypeValues.filter(function (item, pos, self) {
                return self.indexOf(item) == pos;
            });
            deviceTypeCombo.setValue(uniqueDeviceTypeValues);
            deviceStateCombo.setValue(deviceStateValues);
        }
    },

    getValue: function () {
        var me = this,
            deviceTypeCombo = me.down('#device-type'),
            deviceStateCombo = me.down('#device-state');

        var values = deviceStateCombo.getValue();
        if(values.length > 0) {
            var stateStore = me.getDeviceStateStore();
            var deviceTypeValues = deviceTypeCombo.getValue();
            var states = [];
            var uniqueStates = [];
            stateStore.each(function (record) {
                Ext.Array.forEach(deviceTypeValues, function (deviceTypeValue) {
                    if (record.get('deviceId') == deviceTypeValue) {
                        Ext.Array.forEach(values, function (value) {
                            if (record.get('lifeCycleId') == value.split(':')[1]) {
                                var stateId = record.get('deviceId') + ':' + record.get('lifeCycleId') + ':' + value.split(':')[2];
                                states.push(stateId);
                            }
                        });
                    }
                });

            });
            uniqueStates = states.filter(function (item, pos, self) {
                return self.indexOf(item) == pos;
            });
            var formattedResonse = [];
            for (i = 0; i < uniqueStates.length; i++) {
                if (uniqueStates[i] != '') {
                    var value = uniqueStates[i].split(':')[0] + ':' + uniqueStates[i].split(':')[1];
                    var partialState = uniqueStates[i].split(':')[2];
                    for (j = i + 1; j < uniqueStates.length; j++) {
                        if (uniqueStates[j] != '') {
                            if (uniqueStates[j].split(':')[0] + ':' + uniqueStates[j].split(':')[1] == value) {
                                partialState += ',' + uniqueStates[j].split(':')[2];
                                uniqueStates[j] = '';
                            }
                        }
                    }
                    formattedResonse.push(value + ':' + partialState);
                    uniqueStates[i] = '';
                }
            }
            return formattedResonse;
        } else {
            return null;
        }
    },

    getDeviceTypeStore: function () {
        var me = this,
            possibleValues = me.getProperty().getPossibleValues();

        var elements = [];
        if(possibleValues){
            for (var i= 0 ; i< possibleValues.length; i++){
                var object = {};
                object.id = possibleValues[i].id.split(":")[0];
                object.lifeCycleId = possibleValues[i].id.split(":")[1];
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
                },
                {
                    name: 'lifeCycleId'
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
                var lifeCycleStateIDs = possibleValues[i].id.split(":")[2].split(",");
                var lifeCycleStateNames = JSON.parse(possibleValues[i].name).lifeCycleStateName;

                for (var j = 0; j< lifeCycleStateNames.length; j++){
                    var object = {};
                    object.deviceId = possibleValues[i].id.split(":")[0];
                    object.lifeCycleId = possibleValues[i].id.split(":")[1];
                    object.deviceTypeName = JSON.parse(possibleValues[i].name).deviceTypeName;
                    object.id = object.deviceId + ':' + object.lifeCycleId + ':' + lifeCycleStateIDs[j];
                    object.lifeCycleStateName = lifeCycleStateNames[j];
                    elements.push(object);
                }
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
                },
                {
                    name: 'lifeCycleId'
                }
            ],
            data: elements
        });

    }

});
