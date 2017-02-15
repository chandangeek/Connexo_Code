/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Bpm.view.task.bulk.Navigation', {
    extend: 'Uni.view.menu.NavigationMenu',
    alias: 'widget.tasks-bulk-navigation',
    width: 200,
    jumpForward: false,
    jumpBack: true,
    title: Uni.I18n.translate('bpm.task.bulkActions', 'BPM', 'Bulk action'),
    ui: 'medium',
    items: [
        {
            itemId: 'cmbn-select-tasks',
            action: 'select-task',
            text: Uni.I18n.translate('bpm.task.bulk.selectTasks', 'BPM', 'Select tasks')
        },
        {
            itemId: 'cmbn-select-action',
            action: 'select-action',
            text: Uni.I18n.translate('bpm.task.bulk.selectAction', 'BPM', 'Select action')
        },
        {
            itemId: 'cmbn-action-details',
            action: 'select-action',
            text: Uni.I18n.translate('bpm.task.bulk.actionDetails', 'BPM', 'Action details')
        },
        {
            itemId: 'cmbn-confirmation',
            action: 'confirmation',
            text: Uni.I18n.translate('bpm.task.bulk.confirmation', 'BPM', 'Confirmation')
        },
        {
            itemId: 'cmbn-status',
            action: 'status',
            text: Uni.I18n.translate('general.status', 'BPM', 'Status')
        }
    ]
});