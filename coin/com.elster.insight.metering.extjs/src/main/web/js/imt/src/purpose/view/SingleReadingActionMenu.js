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
                itemId: 'copy-form-value',
                text: Uni.I18n.translate('general.copyFromReference', 'IMT', 'Copy from reference'),
                action: 'copyFromReference',
                section: this.SECTION_ACTION
            },
            {
                itemId: 'edit-value',
                text: Uni.I18n.translate('general.edit', 'IMT', 'Edit'),
                action: 'editValue',
                section: this.SECTION_EDIT
            },
            {
                itemId: 'clear-projected',
                hidden: true,
                text: Uni.I18n.translate('general.clearProjectedFlag', 'IMT', 'Clear projected flag'),
                action: 'clearProjectedFlag',
                section: this.SECTION_ACTION
            },
            {
                itemId: 'mark-projected',
                hidden: true,
                text: Uni.I18n.translate('general.markAsProjected', 'IMT', 'Mark as projected'),
                action: 'markProjected',
                section: this.SECTION_ACTION
            },
            {
                itemId: 'correct-value',
                hidden: true,
                text: Uni.I18n.translate('general.correct', 'IMT', 'Correct'),
                action: 'correctValue',
                section: this.SECTION_ACTION
            },
            {
                itemId: 'edit-estimation-comment',
                hidden: true,
                text: Uni.I18n.translate('general.editEstimationComment', 'IMT', 'Edit estimation comment'),
                action: 'editEstimationComment',
                section: this.SECTION_ACTION
            },
            {
                itemId: 'estimate-value',
                text: Uni.I18n.translate('general.editWithEstimator', 'IMT', 'Edit with estimator'),
                action: 'estimateValue',
                section: this.SECTION_ACTION
            },
            {
                itemId: 'estimate-value-with-rule',
                privileges: Imt.privileges.UsagePoint.admin,
                text: Uni.I18n.translate('general.estimateValueWithRule', 'IMT', 'Estimate with rule'),
                action: 'estimateWithRule',
                section: this.SECTION_ACTION
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
