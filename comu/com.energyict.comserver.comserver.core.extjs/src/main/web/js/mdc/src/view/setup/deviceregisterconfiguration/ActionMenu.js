/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.deviceregisterconfiguration.ActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.deviceRegisterConfigurationActionMenu',
    itemId: 'deviceRegisterConfigurationActionMenu',
    initComponent: function () {
        this.items = [
            {
                itemId: 'viewSuspects',
                text: Uni.I18n.translate('deviceregisterconfiguration.menu.viewsuspects', 'MDC', 'View suspects'),
                action: 'viewSuspects',
                section: this.SECTION_VIEW
            },
            {
                itemId: 'validateNowRegister',
                text: Uni.I18n.translate('deviceregisterconfiguration.menu.validate', 'MDC', 'Validate now'),
                privileges: Cfg.privileges.Validation.validateManual,
                action: 'validate',
                section: this.SECTION_ACTION
            },
            {
                itemId: 'editRegister',
                text: Uni.I18n.translate('general.edit', 'MDC', 'Edit'),
                privileges: Mdc.privileges.Device.administrateDevice,
                action: 'edit',
                section: this.SECTION_EDIT
            }
        ];
        this.callParent(arguments);
    },

    listeners: {
        beforeshow: {
            fn: function () {
                if (this.record) {
                    this.down('#validateNowRegister').setVisible(this.record.get('detailedValidationInfo').validationActive);
                }
            }
        }
    }
});
