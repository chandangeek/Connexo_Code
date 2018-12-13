/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Est.estimationrulesets.view.ActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.estimation-rule-sets-action-menu',
    itemId: 'rule-set-action-menu',
    initComponent: function() {
        this.items = [
            {
                itemId: 'edit',
                text: Uni.I18n.translate('general.edit', 'EST', 'Edit'),
                action: 'edit',
                section: this.SECTION_EDIT
            },
            {
                itemId: 'remove',
                text: Uni.I18n.translate('general.remove', 'EST', 'Remove'),
                action: 'remove',
                section: this.SECTION_REMOVE
            }
        ];
        this.callParent(arguments);
    }
});
