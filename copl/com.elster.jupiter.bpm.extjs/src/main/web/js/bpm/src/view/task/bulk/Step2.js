/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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
                    boxLabel: '<b>' + Uni.I18n.translate('task.bulk.taskManagement', 'BPM', 'Task management') + '</b>',
                    afterSubTpl: '<span style="color: grey;padding: 0 0 0 19px;">'
                    + Uni.I18n.translate('task.bulk.actionRadioGroup.taskManagementDescription', 'BPM', 'These actions allow you to manage selected tasks')
                    + '</span>',
                    privileges: Bpm.privileges.BpmManagement.assign,
                    inputValue: 'taskmanagement',
                    checked: true
                },

                {
                    name: 'action',
                    boxLabel: '<b>' + Uni.I18n.translate('task.bulk.taskExecution', 'BPM', 'Task execution') + '</b>',
                    afterSubTpl: '<span style="color: grey;padding: 0 0 0 19px;">'
                    + Uni.I18n.translate('bpm.task.bulk.actionRadioGroup.taskExecutionDescription', 'BPM', 'This action allows you to execute selected tasks')
                    + '</span>',
                    privileges: Bpm.privileges.BpmManagement.execute,
                    inputValue: 'taskexecute'
                }

            ]

        }

    ],
    getManagementActions: function() {
        var arrActions = [];
        if(this.down('#chkg-task-bulk')) {
            if (this.down('#chkg-task-bulk').disabled === false) {
                var arrChk = this.down('#chkg-task-bulk').getChecked();
                Ext.each(arrChk, function (item) {
                    arrActions.push(item.inputValue)
                });
            }
        }
        return arrActions;
    }

});