/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.devicechannels.ActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.deviceLoadProfileChannelsActionMenu',
    itemId: 'deviceLoadProfileChannelsActionMenu',
    initComponent: function () {
        this.items = [
            {
                itemId: 'viewSuspects',
                text: Uni.I18n.translate('deviceregisterconfiguration.menu.viewsuspects', 'MDC', 'View suspects'),
                action: 'viewSuspects',
                section: this.SECTION_VIEW
            },
            {
                itemId: 'validateNowChannel',
                text: Uni.I18n.translate('deviceregisterconfiguration.menu.validate', 'MDC', 'Validate now'),
                privileges: Cfg.privileges.Validation.validateManual,
                dynamicPrivilege: Mdc.dynamicprivileges.DeviceState.validationActions,
                action: 'validateNow',
                section: this.SECTION_ACTION
            },
            {
                itemId: 'editChannel',
                text: Uni.I18n.translate('general.edit', 'MDC', 'Edit'),
                privileges: Mdc.privileges.Device.administrateDevice,
                action: 'edit',
                section: this.SECTION_EDIT
            }
        ];
        this.callParent(arguments);
    }
});
