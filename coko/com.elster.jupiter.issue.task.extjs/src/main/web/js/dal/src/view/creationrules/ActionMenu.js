/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Itk.view.creationrules.ActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.issue-creation-rule-action-menu',
    initComponent: function() {
        this.items = [
            {
                itemId: 'activate',
                text: Uni.I18n.translate('general.activate', 'ITK', 'Activate'),
                privileges: Isu.privileges.Issue.adminRule,
                action: 'activate',
                section: this.SECTION_ACTION
            },
            {
                itemId: 'issue-edit',
                text: Uni.I18n.translate('general.edit', 'ITK', 'Edit'),
                action: 'edit',
                section: this.SECTION_EDIT
            },
            {
                itemId: 'issue-remove',
                text:  Uni.I18n.translate('general.remove', 'ITK', 'Remove'),
                action: 'remove',
                section: this.SECTION_REMOVE
            }
        ];
        this.callParent(arguments);
    }
});
