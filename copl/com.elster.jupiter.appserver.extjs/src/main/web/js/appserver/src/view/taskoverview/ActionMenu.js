/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.view.taskoverview.ActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.task-overview-action-menu',
    //localDomainName: 'Local',
    initComponent: function () {
        this.items = [
            {
                itemId: 'set-queue',
                text: Uni.I18n.translate('general.setqueue', 'APR', 'Set queue'),
                privileges: Usr.privileges.Users.admin,
                action: 'setQueue',
                section: this.SECTION_ACTION
            }
        ];
        this.callParent(arguments);
    }
});