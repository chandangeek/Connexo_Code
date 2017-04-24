/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.devicechannels.DataBulkActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.channel-data-bulk-action-menu',
    initComponent: function () {
        this.items = [
            {
                itemId: 'copy-form-value',
                text: Uni.I18n.translate('general.copyFromReference', 'MDC', 'Copy from reference'),
                action: 'copyFromReference',
                section: this.SECTION_ACTION
            },
            {
                itemId: 'confirm-value',
                hidden: true,
                text: Uni.I18n.translate('general.confirm', 'MDC', 'Confirm'),
                action: 'confirmValue',
                section: this.SECTION_ACTION
            },
            {
                itemId: 'correct-value',
                hidden: true,
                text: Uni.I18n.translate('general.correct', 'MDC', 'Correct'),
                action: 'correctValue',
                section: this.SECTION_ACTION
            },
            {
                itemId: 'estimate-value',
                hidden: true,
                text: Uni.I18n.translate('general.editWithEstimator', 'MDC', 'Edit with estimator'),
                action: 'estimateValue',
                section: this.SECTION_ACTION
            },
            {
                itemId: 'edit-estimation-comment',
                // hidden: true,
                text: Uni.I18n.translate('general.editEstimationComment', 'MDC', 'Edit estimation comment'),
                action: 'editEstimationComment',
                section: this.SECTION_ACTION
            },
            {
                itemId: 'estimate-value-with-rule',
                hidden: true,
                text: Uni.I18n.translate('general.estimateValueWithRule', 'MDC', 'Estimate with rule'),
                action: 'estimateWithRule',
                section: this.SECTION_ACTION
            },
            {
                itemId: 'remove-readings',
                hidden: true,
                text: Uni.I18n.translate('general.removeReadings', 'MDC', 'Remove readings'),
                action: 'removeReadings',
                section: this.SECTION_REMOVE
            },
            {
                itemId: 'view-history',
                text: Uni.I18n.translate('general.viewHistory', 'MDC', 'View history'),
                action: 'viewHistory',
                section: this.SECTION_VIEW
            }
        ];
        this.callParent(arguments);
    }
});
