/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Bpm.view.task.TasksSortMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.bpm-tasks-sort-menu',
    plain: true,
    border: false,
    shadow: false,
    items: [
        {
            text: Uni.I18n.translate('bpm.sort.priority', 'BPM', 'Priority'),
            name: 'priority'
        },
        {
            text: Uni.I18n.translate('bpm.sort.dueDate', 'BPM', 'Due date'),
            name: 'dueDate'
        },
        {
            text: Uni.I18n.translate('bpm.sort.creationDate', 'BPM', 'Creation date'),
            name: 'creationDate'
        }
    ]
});