/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Isu.view.issues.SortingMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.issues-sorting-menu',
    shadow: false,
    border: false,
    plain: true,
    items: [
        {
            itemId: 'issues-sorting-menu-item-by-due-date',
            text: Uni.I18n.translate('general.title.dueDate', 'ISU', 'Due date'),
            action: 'dueDate'
        },
        {
            itemId: 'issues-sorting-menu-item-by-priority',
            text: Uni.I18n.translate('general.title.priority', 'ISU', 'Priority'),
            action: 'priorityTotal'
        }
    ]
});