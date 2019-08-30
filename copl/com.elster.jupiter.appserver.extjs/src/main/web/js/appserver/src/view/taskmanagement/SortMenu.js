/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.view.taskmanagement.SortMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.taskmanagement-sort-menu',
    itemId: 'taskmanagement-sort-menu',
    plain: true,
    border: false,
    shadow: false,
    items: [
        {
            text: Uni.I18n.translate('general.nextRun', 'APR', 'Next run'),
            itemId: 'taskmanagement-sort-menu-nextrun',
            name: 'nextRun'
        },
        {
            text: Uni.I18n.translate('general.queue', 'APR', 'Queue'),
            itemId: 'taskmanagement-sort-menu-queue',
            name: 'queue'
        },
        {
            text: Uni.I18n.translate('general.priority', 'APR', 'Priority'),
            itemId: 'taskmanagement-sort-menu-priority',
            name: 'priority'
        }
    ]
});