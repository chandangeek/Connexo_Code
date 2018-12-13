/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Bpm.view.task.TaskPreview', {
    extend: 'Ext.panel.Panel',
    frame: true,
    alias: 'widget.bpm-task-preview',
    requires: [
        'Bpm.view.task.TaskPreviewForm',
        'Bpm.view.task.TaskActionMenu'
    ],
    tools: [
        {
            xtype: 'uni-button-action',
            privileges: Bpm.privileges.BpmManagement.assignOrExecute,
            menu: {
                xtype: 'bpm-task-action-menu'
            }
        }
    ],

    items: {
        itemId: 'frm-preview-task',
        xtype: 'bpm-task-preview-form'
    }
});

