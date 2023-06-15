/*
 * Copyright (c) 2023 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dal.view.creationrules.ActionsListMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.alarms-creation-rule-action-list-menu',
    initComponent: function () {
        this.items = [
            {
                itemId: 'edit',
                text: Uni.I18n.translate('general.edit', 'DAL', 'Edit'),
                action: 'edit',
                section: this.SECTION_EDIT
            },
            {
                itemId: 'remove',
                text: Uni.I18n.translate('general.remove', 'DAL', 'Remove'),
                action: 'remove',
                section: this.SECTION_REMOVE
            }
        ];
        this.callParent(arguments);
    }
});