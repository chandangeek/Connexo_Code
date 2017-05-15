/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.purpose.view.registers.RegisterReadingActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.purpose-register-readings-data-action-menu',
    router: null,
    initComponent: function() {
        this.items = [
            {
                itemId: 'confirm-value',
                hidden: true,
                text: Uni.I18n.translate('general.confirm', 'IMT', 'Confirm'),
                action: 'confirmValue',
                section: this.SECTION_ACTION
            },
            {
                itemId: 'view-history',
                text: Uni.I18n.translate('general.viewHistory', 'IMT', 'View history'),
                action: 'viewHistory',
                section: this.SECTION_ACTION
            },
            {
                itemId: 'edit-value',
                text: Uni.I18n.translate('general.edit', 'IMT', 'Edit'),
                action: 'editValue',
                section: this.SECTION_EDIT
            },
            {
                itemId: 'reset-value',
                hidden: true,
                text: Uni.I18n.translate('general.restore', 'IMT', 'Restore'),
                action: 'resetValue',
                section: this.SECTION_REMOVE
            }
        ];
        this.callParent(arguments);
    }

});
