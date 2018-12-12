/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Bpm.view.task.bulk.CompleteTaskForm', {
    extend: 'Uni.view.container.ContentContainer',
    requires: [
        'Ext.form.Panel',
        'Bpm.store.task.Tasks',
        'Bpm.view.task.bulk.TaskGroupsGrid',
        'Bpm.view.task.bulk.TaskGroupPreview'

    ],
    ui: 'medium',
    padding: 0,
    alias: 'widget.task-complete-form',

    router: null,
    initComponent: function () {
        var me = this;

        me.content = {
            xtype: 'panel',
            itemid: 'bpm-tasks-complete-form',
            ui: 'large',

            items: [
                {
                    xtype: 'component',
                    itemId: 'mandatory-fields-error',
                    cls: 'x-form-invalid-under',
                    margin: '0 0 0 0',
                    html: Uni.I18n.translate('bpm.task.bulk.mandatoryFieldsError', 'BPM', 'You need to input values for mandatory fields'),
                    hidden: true
                },
                {
                    xtype: 'preview-container',
                    grid: {
                        xtype: 'bpm-tasks-occurences-grid',
                        itemId: 'tasks-complete-grid',
                        router: me.router
                    },

                    emptyComponent: {
                        xtype: 'no-items-found-panel',
                        itemId: 'bpm-tasks-empty-grid',
                        title: Uni.I18n.translate('bpm.task.empty.title', 'BPM', 'No tasks found'),
                        reasons: [
                            Uni.I18n.translate('bpm.task.empty.list.item1', 'BPM', 'No tasks have been created yet.'),
                            Uni.I18n.translate('bpm.task.empty.list.item2', 'BPM', 'Tasks exist, but you do not have permission to view them.'),
                            Uni.I18n.translate('bpm.task.empty.list.item3', 'BPM', 'No tasks comply with the filter.'),
                            Uni.I18n.translate('bpm.task.empty.list.item4', 'BPM', 'Connexo Flow is not available.')
                        ]
                    },

                    previewComponent: {
                        xtype: 'bpm-task-group-preview',
                        itemId: 'frm-task-execute'
                    }
                }
            ]
        };
        me.callParent(arguments);
    }
});