/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */


Ext.define('Uni.property.view.property.Task', {
    extend: 'Uni.property.view.property.Base',

    selType: 'checkboxmodel',

    getEditCmp: function () {
        var me = this;
        me.layout = 'vbox';
        return [
            {
                items: [
                    {
                        xtype: 'uni-grid-filtertop-combobox',
                        itemId: 'task-type',
                        fieldLabel: Uni.I18n.translate('task.type', 'UNI', 'Task Type'),
                        queryMode: 'local',
                        name: 'taskType',
                        labelWidth: 260,
                        width: 595,
                        emptyText: Uni.I18n.translate('general.device.type.empty', 'UNI', 'Select a task type...'),
                        valueField: 'id',
                        displayField: 'destinationName',
                        allowBlank: false,
                        multiSelect: true,
                        store: me.getTaskTypeStore(),
                        msgTarget: 'under',
                        editable: false,
                        required: me.property.get('required'),
                        listeners: {
                            change: function (combo, newValue, oldValue) {
                                var taskNameCombo = me.down('#task-name');
                                var store = me.getTaskNameStore();
                                var elements = [];
                                store.each(function (record) {

                                    var result = elements.filter(function (element) {
                                        return element.recurrentTaskName == record.get('recurrentTaskName') &&
                                            element.destinationName == record.get('destinationName');
                                    });

                                    if (result.length == 0) {
                                        var object = {};
                                        object.recurrentTaskName = record.get('recurrentTaskName');
                                        object.id = record.get('id');
                                        object.destinationName = record.get('destinationName');
                                        elements.push(object);
                                    }
                                });

                                var filteredStore = Ext.create('Ext.data.JsonStore', {
                                    fields: ['id', 'recurrentTaskName', 'destinationName'],
                                    sorters: [{
                                        property: 'recurrentTaskName',
                                        direction: 'ASC'
                                    }],
                                    groupField: 'id',
                                    data: elements
                                });
                                taskNameCombo.queryFilter = null;
                                if (elements.length == 0) {
                                    taskNameCombo.clearValue();
                                    taskNameCombo.bindStore(null);
                                    taskNameCombo.setDisabled(true);

                                } else {
                                    taskNameCombo.bindStore(filteredStore);
                                    if (taskNameCombo.getValue().length > 0)
                                        taskNameCombo.setValue(taskNameCombo.getValue().filter(function (value) {
                                            return newValue.indexOf(value) != -1;
                                        }));
                                    taskNameCombo.setDisabled(false);
                                }
                            }
                        }
                    },
                    {
                        xtype: 'uni-grid-filtertop-combobox',
                        itemId: 'task-name',
                        enabled: false,
                        disabled: true,
                        fieldLabel: '&nbsp',
                        queryMode: 'local',
                        name: this.getName(),
                        labelWidth: 260,
                        allowBlank: false,
                        width: 595,
                        valueField: 'id',
                        displayField: 'recurrentTaskName',
                        store: me.getTaskNameStore(),
                        editable: false,
                        multiSelect: true,
                        msgTarget: 'under',
                        emptyText: Uni.I18n.translate('general.task.name.empty', 'UNI', 'Select a task name...')
                    }
                ]
            }
        ];
    },

    setLocalizedName: function (name) {
    },

    setValue: function (value) {
        var me = this,
            taskTypeValues = [],
            taskNameValues = [],
            taskTypeCombo = me.down('#task-type'),
            taskNameCombo = me.down('#task-name');

        if (value) {
            value = [].concat(value);
            Ext.Array.forEach(value, function (item) {
                if (item !== '') {
                    taskNameValues.push(item);
                }
            });
            var uniqueDeviceTypeValues = taskTypeValues.filter(function (item, pos, self) {
                return self.indexOf(item) == pos;
            });
            taskTypeCombo.setValue(uniqueDeviceTypeValues);
            taskNameCombo.setValue(taskNameValues);
        }
    },

    getValue: function () {
        var me = this,
            taskTypeCombo = me.down('#task-type'),
            taskNameCombo = me.down('#task-name');

        var values = taskNameCombo.getValue();

        if (values.length > 0) {
            var taskNameStore = me.getTaskNameStore();
            var taskTypeValues = taskTypeCombo.getValue();
            var names = [];
            var unique = [];
            taskNameStore.each(function (record) {
                Ext.Array.forEach(taskTypeValues, function (taskTypeValue) {
                    if (record.get('id') == taskTypeValue) {
                        Ext.Array.forEach(values, function (value) {
                            names.push(value);
                        });
                    }
                });

                unique = names.filter(function (item, pos, self) {
                    return self.indexOf(item) == pos;
                });

            });
            return unique;
        } else {
            return null;
        }
    },

    getTaskTypeStore: function () {
        var me = this,
            possibleValues = me.getProperty().getPossibleValues();

        var elements = [];
        if (possibleValues) {
            for (var i = 0; i < possibleValues.length; i++) {
                var object = {};
                object.id = possibleValues[i].id;
                object.destinationName = JSON.parse(possibleValues[i].name).destinationName;
                elements.push(object);
            }
        }
        return Ext.create('Ext.data.JsonStore', {
            fields: [
                {
                    name: 'id'
                },
                {
                    name: 'destinationName'
                }
            ],
            data: elements
        });

    },

    getTaskNameStore: function () {
        var me = this,
            possibleValues = me.getProperty().getPossibleValues();
        var elements = [];
        if (possibleValues) {
            for (var i = 0; i < possibleValues.length; i++) {
                var object = {};
                object.destinationName = JSON.parse(possibleValues[i].name).destinationName;
                object.recurrentTaskName = JSON.parse(possibleValues[i].name).recurrentTaskName;
                object.id = possibleValues[i].id;
                elements.push(object);
            }
        }
        return Ext.create('Ext.data.JsonStore', {
            fields: [
                {
                    name: 'id'
                },
                {
                    name: 'destinationName'
                },
                {
                    name: 'recurrentTaskName'
                }
            ],
            data: elements
        });

    }

});
