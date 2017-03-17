/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.devicechannels.DataActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.deviceLoadProfileChannelDataActionMenu',
    estimationRulesCount: null,
    initComponent: function () {
        this.items = [
            {
                itemId: 'viewHistory',
                text: Uni.I18n.translate('deviceloadprofiles.viewHistory', 'MDC', 'View history'),
                action: 'viewHistory',
                hidden: true,
                section: this.SECTION_VIEW
            },
            {
                itemId: 'edit-value',
                text: Uni.I18n.translate('general.edit', 'MDC', 'Edit'),
                action: 'editValue',
                dynamicPrivilege: Mdc.dynamicprivileges.DeviceState.deviceDataEditActions,
                section: this.SECTION_EDIT
            },
            {
                itemId: 'confirm-value',
                hidden: true,
                text: Uni.I18n.translate('general.confirm', 'MDC', 'Confirm'),
                action: 'confirmValue',
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
                itemId: 'estimate-value-with-rule',
                hidden: true,
                text: Uni.I18n.translate('general.estimateValueWithRule', 'MDC', 'Estimate with rule'),
                action: 'estimateWithRule',
                section: this.SECTION_ACTION
            },
            {
                itemId: 'remove-reading',
                text: Uni.I18n.translate('general.remove', 'MDC', 'Remove'),
                action: 'removeReading',
                dynamicPrivilege: Mdc.dynamicprivileges.DeviceState.deviceDataEditActions,
                section: this.SECTION_REMOVE
            }
        ];
        this.callParent(arguments);
    }
});
