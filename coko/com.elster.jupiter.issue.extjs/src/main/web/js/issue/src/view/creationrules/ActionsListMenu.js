/*
 * Copyright (c) 2023 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Isu.view.creationrules.ActionsListMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.creation-rule-action-list-menu',
    initComponent: function () {
        this.items = [
            {
                itemId: 'edit',
                text: Uni.I18n.translate('general.edit', 'ISU', 'Edit'),
                action: 'edit',
                section: this.SECTION_EDIT
            },
            {
                itemId: 'remove',
                text: Uni.I18n.translate('general.remove', 'ISU', 'Remove'),
                action: 'remove',
                section: this.SECTION_REMOVE
            }
        ];
        this.callParent(arguments);
    }
});