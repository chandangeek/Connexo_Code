Ext.define('Bpm.view.task.bulk.Step2', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.tasks-bulk-step2',
    items: [
        {
            xtype: 'uni-form-error-message',
            itemId: 'step2-error-message',
            width: 400,
            hidden: true
        },
        {
            xtype: 'radiogroup',
            itemId: 'tasks-bulk-action-radiogroup',
            columns: 1,
            vertical: true,
            submitValue: false,
            defaults: {
                padding: '0 0 16 0'
            },
            items: [

                {
                    name: 'action',
                    boxLabel: '<b>' + Uni.I18n.translate('general.taskManagement', 'BPM', 'Task management') + '</b>',
                    afterSubTpl: '<span style="color: grey;padding: 0 0 0 19px;">'
                    + Uni.I18n.translate('task.bulk.actionRadioGroup.taskManagementDescription', 'BPM', 'The selected tasks will be managed.')
                    + '</span>',
                    inputValue: 'taskmanagement',
                    checked: true
                },
                {
                    xtype: 'checkboxgroup',
                    align: 'right',
                    padding: '0 0 16 40',
                    itemId:'chkg-task-bulk',
                    columns: 1,
                    vertical: true,
                    labelAlign: 'top',
                    items: [
                        {
                            xtype: 'checkbox',
                            boxLabel: 'Assign tasks',
                            name: 'rbTaskManagement',
                            inputValue: 'assign' },
                        {
                            xtype: 'checkbox',
                            boxLabel: 'Set due date',
                            name: 'rbTaskManagement',
                            inputValue: 'setDueDate' },
                        {
                            xtype: 'checkbox',
                            boxLabel: 'Set priority',
                            name: 'rbTaskManagement',
                            inputValue: 'setPriority' }
                    ]
                },
                {
                    xtype: 'component',
                    itemId: 'action-selection-error',
                    cls: 'x-form-invalid-under',
                    margin: '0 0 0 40',
                    html: Uni.I18n.translate('task.bulk.MgmtActionSelectionError', 'BPM', 'Select at least one action!'),
                    hidden: true
                },
                {
                    name: 'action',
                    boxLabel: '<b>' + Uni.I18n.translate('general.taskExecution', 'BPM', 'Task execution') + '</b>',
                    afterSubTpl: '<span style="color: grey;padding: 0 0 0 19px;">'
                    + Uni.I18n.translate('task.bulk.actionRadioGroup.taskExecutionDescription', 'BPM', 'The selected tasks will be executed.')
                    + '</span>',
                    inputValue: 'taskexecute'
                },
                {
                    xtype: 'radiogroup',
                    itemId: 'tasks-bulk-execute-radiogroup',
                    columns: 1,
                    vertical: true,
                    submitValue: false,
                    disabled:true,
                    defaults: {
                        padding: '0 0 0 40'
                    },
                    items: [
                        {
                            name: 'execute-action',
                            boxLabel: Uni.I18n.translate('task.bulk.completeTask', 'BPM', 'Complete task'),
                            inputValue: 'complete',
                            checked: true
                        },
                    ]
                }

            ],
            listeners: {
                change: function(radiogroup, radio) {
                    if (radio.action == 'taskexecute'){
                        this.down('#chkg-task-bulk').disable();
                        this.down('#tasks-bulk-execute-radiogroup').enable();

                    }
                    else{
                        this.down('#chkg-task-bulk').enable();
                        this.down('#tasks-bulk-execute-radiogroup').disable();
                    }

                }
            }
        }

    ],
    getManagementActions: function() {
        var arrActions = [];
        if(this.down('#chkg-task-bulk').disabled === false) {
            var arrChk = this.down('#chkg-task-bulk').getChecked();
            Ext.each(arrChk, function (item) {
                arrActions.push(item.inputValue)
            });
        }
        return arrActions;
    }
});