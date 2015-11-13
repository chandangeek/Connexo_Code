Ext.define('Bpm.view.task.bulk.Step2', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.tasks-bulk-step2',
    items: [
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
                        { boxLabel: 'Assign tasks', name: 'rb', inputValue: '1' },
                        { boxLabel: 'Set due date', name: 'rb', inputValue: '2' },
                        { boxLabel: 'Set priority', name: 'rb', inputValue: '3' }
                    ]
                },
                {
                    name: 'action',
                    boxLabel: '<b>' + Uni.I18n.translate('general.taskExecution', 'BPM', 'Task execution') + '</b>',
                    afterSubTpl: '<span style="color: grey;padding: 0 0 0 19px;">'
                    + Uni.I18n.translate('task.bulk.actionRadioGroup.taskExecutionDescription', 'BPM', 'The selected tasks will be executed.')
                    + '</span>',
                    inputValue: 'taskexecute'
                }
            ],
            listeners: {
                change: function(radiogroup, radio) {
                    if (radio.action == 'taskexecute'){
                        this.down('#chkg-task-bulk').disable();
                    }
                    else{
                        this.down('#chkg-task-bulk').enable();
                    }

                }
            }
        }

    ]
});