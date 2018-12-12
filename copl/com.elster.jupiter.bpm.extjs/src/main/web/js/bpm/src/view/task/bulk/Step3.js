/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Bpm.view.task.bulk.Step3', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.tasks-bulk-step3',
    requires: [
        'Bpm.view.task.bulk.ManageTaskForm',
        'Bpm.view.task.bulk.CompleteTaskForm'
    ],
    html: '',
    margin: '0 0 0 0',
    items: [
        {
            xtype: 'uni-form-error-message',
            itemId: 'step3-error-message',
            width: 430,
            margin: '0 0 5 0',
            hidden: true
        },
        {
            xtype: 'task-manage-form',
            itemId: 'bpm-tasks-bulk-attributes-form',
            isMultiEdit: true,
            width: '100%'
        },
        {
            xtype: 'task-complete-form',
            itemId: 'bpm-tasks-bulk-complete-form',
            isMultiEdit: true,
            width: '100%'
        }

    ],

    setControls: function(taskActions) {
        var me = this;
        Ext.each(taskActions, function (item) {
            me.down('fieldcontainer[name=' + item + ']').setVisible(true);
        });
    },
    setForms: function(operation) {
        if(operation === 'taskmanagement')
        {
            this.down('#bpm-tasks-bulk-attributes-form').setVisible(true);
            this.down('#bpm-tasks-bulk-complete-form').setVisible(false);
        }
        else
        {
            this.down('#bpm-tasks-bulk-attributes-form').setVisible(false);
            this.down('#bpm-tasks-bulk-complete-form').setVisible(true);
        }
    },
    getManagementActions: function() {
        var me = this;
        var arrActions = [];

        if (!me.down('combo[itemId=cbo-user-assignee]').disabled)
            arrActions.push('userAssign');
        if (!me.down('combo[itemId=cbo-workgroup-assignee]').disabled)
            arrActions.push('workgroupAssign');
        if(!me.down('fieldcontainer[name=setDueDate]').disabled)
            arrActions.push('setDueDate');
        if(!me.down('fieldcontainer[name=setPriority]').disabled)
            arrActions.push('setPriority');

        return arrActions;
    }
});