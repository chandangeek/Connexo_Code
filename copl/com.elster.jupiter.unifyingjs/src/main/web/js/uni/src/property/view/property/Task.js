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
                        fieldLabel: Uni.I18n.translate('task.types', 'UNI', 'Tasks'),
                        queryMode: 'local',
                        name: 'taskType',
                        labelWidth: 260,
                        width: 595,
                        emptyText: Uni.I18n.translate('task.types.empty', 'UNI', 'Select a queue ...'),
                        valueField: 'destinationName',
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
                                var filterByTaskTypes = [];
                                Ext.Array.forEach(newValue, function (destinationName) {
                                    store.each(function (record) {
                                        if (record.get('destinationName') == destinationName) {
                                            filterByTaskTypes.push(record);
                                        }
                                    });
                                });

                                var filteredStore = Ext.create('Ext.data.JsonStore', {
                                    fields: ['id', 'recurrentTaskName', 'destinationName'],
                                    sorters: [{
                                        property: 'recurrentTaskName',
                                        direction: 'ASC'
                                    }],
                                    data: filterByTaskTypes
                                });
                                taskNameCombo.queryFilter = null;
                                if (filterByTaskTypes.length == 0) {
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

    setValue: function (values) {
        var me = this,
            possibleValues = me.getProperty().getPossibleValues(),
            taskTypeValues = [],
            taskNameValues = [],
            taskTypeCombo = me.down('#task-type'),
            taskNameCombo = me.down('#task-name');

        if (values) {
            values = [].concat(values);
            taskNameValues = values;

            Ext.Array.forEach(values, function (value) {
                var destinationName = '';
                var selectedValues = Ext.Array.findBy(possibleValues, function (possibleValue) {
                    return possibleValue.id == value;
                });

                if (selectedValues){
                    selectedValues = [].concat(selectedValues);
                    destinationName = JSON.parse(selectedValues[0].name).destinationName;

                    var selectedTaskType = Ext.Array.findBy(taskTypeValues, function (taskTypeValue) {
                        return taskTypeValue === destinationName;
                    });
                    if (!selectedTaskType){
                        taskTypeValues.push(destinationName);
                    }
                }
            });
            taskTypeCombo.setValue(taskTypeValues);
            taskNameCombo.setValue(taskNameValues);
        }
    },

    getValue: function () {
        var me = this,
            taskNameCombo = me.down('#task-name');

        if (taskNameCombo == null || taskNameCombo.getValue() == null || taskNameCombo.getValue().length == 0){
            return null;
        }
       return taskNameCombo.getValue();
    },

    getTaskTypeStore: function () {
        var me = this,
            possibleValues = me.getProperty().getPossibleValues();

        var elements = [];
        if (possibleValues) {
            Ext.Array.forEach(possibleValues, function (possibleValue) {
                elements.push({destinationName: JSON.parse(possibleValue.name).destinationName} );
            });
        }

        var unique = [], uniqueByDestinations = [], l = elements.length, i;
        for( i=0; i<l; i++) {
            if( unique[elements[i].destinationName]) continue;
            unique[elements[i].destinationName] = true;
            uniqueByDestinations.push(elements[i]);
        }

        return Ext.create('Ext.data.JsonStore', {
            fields: [
                {
                    name: 'destinationName'
                }
            ],
            sorters: [
                {
                    property: 'destinationName',
                    direction: 'ASC'
                }
            ],
            data: uniqueByDestinations
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
            sorters: [
                {
                    property: 'recurrentTaskName',
                    direction: 'ASC'
                }
            ],
            data: elements
        });
    }
});
