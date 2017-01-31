/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.view.appservers.ActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.appservers-action-menu',
    initComponent: function () {
        this.items =
            [
                {
                    itemId: 'activate-appserver',
                    text: Uni.I18n.translate('general.activate', 'APR', 'Activate'),
                    privileges: Apr.privileges.AppServer.admin,
                    action: 'activateAppServer',
                    section: this.SECTION_ACTION
                },
                {
                    itemId: 'edit-appserver',
                    text: Uni.I18n.translate('general.edit', 'APR', 'Edit'),
                    privileges: Apr.privileges.AppServer.admin,
                    action: 'editAppServer',
                    section: this.SECTION_EDIT
                },
                {
                    itemId: 'remove-appserver',
                    text: Uni.I18n.translate('general.remove', 'APR', 'Remove'),
                    privileges: Apr.privileges.AppServer.admin,
                    action: 'removeAppServer',
                    section: this.SECTION_REMOVE
                }
            ];
        this.callParent(arguments);
    }
});