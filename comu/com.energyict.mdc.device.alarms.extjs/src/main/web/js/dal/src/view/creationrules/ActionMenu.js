/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dal.view.creationrules.ActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.alarm-creation-rule-action-menu',
    initComponent: function() {
        this.items = [
            {
                itemId: 'alarm-edit',
                text: Uni.I18n.translate('general.edit', 'DAL', 'Edit'),
                action: 'edit',
                section: this.SECTION_EDIT
            },
            {
                itemId: 'alarm-remove',
                text:  Uni.I18n.translate('general.remove', 'DAL', 'Remove'),
                action: 'remove',
                section: this.SECTION_REMOVE
            }
        ];
        this.callParent(arguments);
    }
});
