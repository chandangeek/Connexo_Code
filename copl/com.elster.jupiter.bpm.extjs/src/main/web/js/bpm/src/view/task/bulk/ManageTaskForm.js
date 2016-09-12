Ext.define('Bpm.view.task.bulk.ManageTaskForm', {
    extend: 'Ext.form.Panel',
    requires: [
        'Ext.form.Panel',
        'Bpm.store.task.TasksUsers'
    ],
    ui: 'medium',
    padding: 0,
    alias: 'widget.task-manage-form',
    items: [
        {
            xtype: 'form',
            ui: 'large',
            margin: '0 0 10 0',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            defaults: {
                width: 800
            },
            items: [
                {
                    xtype: 'container',
                    layout: {
                        type: 'hbox',
                        align: 'center'
                    },
                    items: [

                        {
                            xtype: 'checkbox',
                            itemId: 'task-bulk-assignee-check',
                            name: 'cbAssignee',
                            inputValue: 'assign',
                            listeners: {
                                change: function() {
                                    if(this.value)
                                        this.up('form').down('fieldcontainer[name=assign]').enable();
                                    else
                                        this.up('form').down('fieldcontainer[name=assign]').disable();
                                }
                            }
                        },
                        {
                            xtype: 'fieldcontainer',
                            name: 'assign',
                            disabled: true,
                            margin: '0 10 0 0',
                            width: 800,
                            layout: 'hbox',
                            items: [

                                {
                                    xtype: 'combobox',
                                    itemId: 'cbo-bulk-task-assignee',
                                    fieldLabel: Uni.I18n.translate('general.assignee', 'BPM', 'Assignee'),
                                    required: true,
                                    queryMode: 'local',
                                    margin: '0 10 0 0',
                                    valueField: 'id',
                                    allowBlank: false,
                                    validateOnChange: false,
                                    name: 'assigneeCombo',
                                    emptyText: Uni.I18n.translate('bpm.task.startTypingForUsers', 'BPM', 'Start typing for users'),
                                    displayField: 'name'
                                }

                            ]
                        }
                    ]
                },
                {
                    xtype: 'component',
                    itemId: 'user-selection-error',
                    cls: 'x-form-invalid-under',
                    margin: '0 0 0 40',
                    html: Uni.I18n.translate('task.bulk.MgmtAsignneSelectionError', 'BPM', 'You must choose user before you can proceed'),
                    hidden: true
                },
                {
                    xtype: 'container',
                    layout: {
                        type: 'hbox',
                        align: 'center'
                    },
                    items: [
                        {
                            xtype: 'checkbox',
                            itemId: 'task-bulk-due-date-check',
                            margin: '10 0 0 0',
                            name: 'cbDuedate',
                            inputValue: 'dueDate',
                            listeners: {
                                change: function() {
                                    if(this.value)
                                        this.up('form').down('fieldcontainer[name=setDueDate]').enable();
                                    else
                                        this.up('form').down('fieldcontainer[name=setDueDate]').disable();
                                }
                            }
                        },
                        {
                            xtype: 'fieldcontainer',
                            fieldLabel: Uni.I18n.translate('bpm.task.bulk.dueDate', 'BPM', 'Due date'),
                            name: 'setDueDate',
                            disabled: true,
                            margin: '10 0 10 0',
                            layout: 'hbox',
                            items: [
                                {
                                    xtype: 'date-time',
                                    itemId: 'task-due-date',
                                    layout: 'hbox',
                                    name: 'start-on',
                                    dateConfig: {
                                        allowBlank: true,
                                        value: new Date(),
                                        editable: false,
                                        format: Uni.util.Preferences.lookup(Uni.DateTime.dateShortKey, Uni.DateTime.dateShortDefault)
                                    },
                                    hoursConfig: {
                                        fieldLabel: Uni.I18n.translate('general.at', 'BPM', 'at'),
                                        labelWidth: 10,
                                        margin: '0 0 0 10',
                                        value: new Date().getHours()
                                    },
                                    minutesConfig: {
                                        width: 55,
                                        value: new Date().getMinutes()
                                    }
                                }
                            ]
                        }
                    ]
                },
                {
                    xtype: 'container',
                    layout: {
                        type: 'hbox',
                        align: 'center'
                    },
                    items: [
                        {
                            xtype: 'checkbox',
                            itemId: 'task-bulk-priority-check',
                            name: 'cbPriority',
                            inputValue: 'priority',
                            listeners: {
                                change: function() {
                                    if(this.value)
                                        this.up('form').down('fieldcontainer[name=setPriority]').enable();
                                    else
                                        this.up('form').down('fieldcontainer[name=setPriority]').disable();
                                }
                            }
                        },
                        {
                            itemId: 'priority-values',
                            xtype: 'fieldcontainer',
                            fieldLabel: Uni.I18n.translate('bpm.task.priority', 'BPM', 'Priority'),
                            name: 'setPriority',
                            disabled: true,
                            margin: '0 0 10 0',
                            layout: 'hbox',

                            items: [
                                {
                                    itemId: 'num-priority-number',
                                    xtype: 'numberfield',
                                    name: 'recurrence-number',
                                    allowDecimals: false,
                                    minValue: 0,
                                    maxValue: 10,
                                    value: 0,
                                    width: 65,
                                    margin: '0 10 0 0',
                                    listeners: {
                                        change: function (numberfield, value) {
                                            var labelString;


                                            if (value <= 3) {
                                                labelString = Uni.I18n.translate('bpm.task.priority.high', 'BPM', 'High');
                                            }
                                            else if (value <= 7) {
                                                labelString = Uni.I18n.translate('bpm.task.priority.medium', 'BPM', 'Medium');
                                            }
                                            else {
                                                labelString = Uni.I18n.translate('bpm.task.priority.low', 'BPM', 'Low');
                                            }
                                            this.up('form').down('#txt-priority').setText(labelString);
                                        }
                                    }
                                },
                                {
                                    xtype: 'label',
                                    layout: 'fit',
                                    width: '100%',
                                    itemId: 'txt-priority',
                                    text: Uni.I18n.translate('bpm.task.priority.high', 'BPM', 'High')
                                }


                            ]
                        }
                    ]
                },
                {
                    xtype: 'component',
                    itemId: 'controls-selection-error',
                    cls: 'x-form-invalid-under',
                    margin: '0 0 0 40',
                    html: Uni.I18n.translate('task.bulk.MgmtControlsSelectionError', 'BPM', 'You must enable a field before you can proceed'),
                    hidden: true
                }

            ]
        }
    ],
    initComponent: function() {
        var me = this,
            userStore = Ext.getStore('Bpm.store.task.TasksUsers'),
            assigneeCombo;

        me.callParent(arguments);

        assigneeCombo = me.down('combobox[name=assigneeCombo]');
        userStore.load(function (records) {
            Ext.getBody().unmask();
            if (!Ext.isEmpty(records)) {
                assigneeCombo.bindStore(userStore);
            }
        });

    }
});