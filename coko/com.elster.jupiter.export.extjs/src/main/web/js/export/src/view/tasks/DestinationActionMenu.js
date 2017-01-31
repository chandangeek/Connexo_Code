/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dxp.view.tasks.DestinationActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.dxp-tasks-destination-action-menu',
    initComponent: function() {
        this.items = [
            {
                itemId: 'edit-destination',
                text: Uni.I18n.translate('general.edit', 'DES', 'Edit'),
                privileges: Dxp.privileges.DataExport.update,
                action: 'editDestination',
                section: this.SECTION_EDIT
            },
            {
                itemId: 'remove-destination',
                text: Uni.I18n.translate('general.remove', 'DES', 'Remove'),
                privileges: Dxp.privileges.DataExport.admin,
                action: 'removeDestination',
                section: this.SECTION_REMOVE
            }
        ];
        this.callParent(arguments);
    }
});

