/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mtr.view.readingtypes.SortingMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.reading-types-sorting-menu',
    shadow: false,
    border: false,
    plain: true,
    items: [
        {
            itemId: 'reading-types-sorting-menu-item-by-name',
            text: Uni.I18n.translate('readingtypesmanagement.name', 'MTR', 'Name'),
            name: 'fullAliasName'
        },
        {
            itemId: 'reading-types-sorting-menu-item-by-status',
            text: Uni.I18n.translate('readingtypesmanagement.status', 'MTR', 'Status'),
            name: 'active'
        }
    ]
});