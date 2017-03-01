/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.purpose.view.SingleReadingActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.purpose-readings-data-action-menu',
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
                itemId: 'estimate-value',
                hidden: true,
                text: Uni.I18n.translate('general.estimate', 'IMT', 'Estimate'),
                action: 'estimateValue',
                section: this.SECTION_ACTION
            },
            {
                itemId: 'estimate-value-with-rule',
                privileges: Imt.privileges.UsagePoint.admin,
                hidden: true,
                text: Uni.I18n.translate('general.estimateValueWithRule', 'IMT', 'Estimate with rule'),
                action: 'estimateWithRule',
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
                text: Uni.I18n.translate('general.restoreReadings', 'IMT', 'Restore'),
                action: 'resetValue',
                section: this.SECTION_REMOVE
            }
        ];

        this.callParent(arguments);
    }
});
