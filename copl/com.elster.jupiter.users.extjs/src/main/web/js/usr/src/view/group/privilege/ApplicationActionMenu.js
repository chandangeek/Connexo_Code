/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Usr.view.group.privilege.ApplicationActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.application-action-menu',
    plain: true,
    border: false,
    itemId: 'application-action-menu',
    shadow: false,
    items: [
        {
            text: Uni.I18n.translate('privilege.noAccess', 'USR', 'No access'),
            icon: '../sky/build/resources/images/grid/drop-no.png',
            itemId: 'privilegeNoAccess',
            action: 'privilegeNoAccess',
            iconCls: 'x-menu-item-checkbox'
        },
        {
            text: Uni.I18n.translate('privilege.fullControl', 'USR', 'Full control'),
            icon: '../sky/build/resources/images/grid/drop-yes.png',
            itemId: 'privilegeFullControl',
            action: 'privilegeFullControl',
            iconCls: 'x-menu-item-checkbox'
        }
    ]
});
