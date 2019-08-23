/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.view.taskoverview.SortMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.taskoverview-sort-menu',
    itemId: 'taskoverview-sort-menu',
    plain: true,
    border: false,
    shadow: false,
    items: [
        {
            text: Uni.I18n.translate('general.nextRun', 'APR', 'Next run'),
            name: 'nextRun'
        },
        {
            text: Uni.I18n.translate('general.queue', 'APR', 'Queue'),
            name: 'queue'
        },
        {
            text: Uni.I18n.translate('general.priority', 'APR', 'Priority'),
            name: 'priority'
        }
    ]
});