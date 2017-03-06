/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.devicechannels.DataActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.deviceLoadProfileChannelDataActionMenu',
    initComponent: function () {
        this.items = [
            {
                itemId: 'viewHistory',
                text: Uni.I18n.translate('deviceloadprofiles.viewHistory', 'MDC', 'View history'),
                action: 'viewHistory',
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
                text: Uni.I18n.translate('general.estimate', 'MDC', 'Estimate'),
                action: 'estimateValue',
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
    },

    listeners: {
        beforeshow: function (menu) {
            var validationResult = menu.record.get('validationResult'),
                mainStatus = false,
                bulkStatus = false;

            if (validationResult) {
                mainStatus = validationResult.main === 'suspect';
                bulkStatus = validationResult.bulk === 'suspect';
            }

            menu.down('#estimate-value').setVisible(mainStatus || bulkStatus);
            if (menu.record.get('confirmed') || menu.record.isModified('value') || menu.record.isModified('collectedValue')) {
                menu.down('#confirm-value').hide();
            } else {
                menu.down('#confirm-value').setVisible(mainStatus || bulkStatus);
            }

            if (menu.down('#remove-reading')) {
                menu.down('#remove-reading').setVisible(menu.record.get('value') || menu.record.get('collectedValue'));
            }
        }
    }
});
