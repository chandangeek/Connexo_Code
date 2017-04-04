/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Isu.view.creationrules.ActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.creation-rule-action-menu',
    initComponent: function() {
        this.items = [
            {
                itemId: 'activate',
                text: Uni.I18n.translate('general.activate', 'ISU', 'Activate'),
                privileges: Isu.privileges.Issue.adminRule,
                action: 'activate',
                section: this.SECTION_ACTION
            },
            {
                itemId: 'edit',
                text: Uni.I18n.translate('general.edit', 'ISU', 'Edit'),
                action: 'edit',
                section: this.SECTION_EDIT
            },
            {
                itemId: 'remove',
                text:  Uni.I18n.translate('general.remove', 'ISU', 'Remove'),
                action: 'remove',
                section: this.SECTION_REMOVE
            }
        ];
        this.callParent(arguments);
    }
});
