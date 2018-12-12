/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.devicecommand.widget.ActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.device-command-action-menu',
    deviceId: null,
    record: null,
    device: null,
    itemId: 'device-command-action-menu',
    initComponent: function () {
        this.items = [
            {
                text: Uni.I18n.translate('deviceCommand.actionMenu.trigger', 'MDC', 'Trigger now'),
                itemId: 'triggerNow',
                action: 'trigger',
                section: this.SECTION_ACTION
            },
            {
                text: Uni.I18n.translate('deviceCommand.actionMenu.changeReleaseDate', 'MDC', 'Change release date'),
                action: 'changeReleaseDate',
                section: this.SECTION_EDIT
            },
            {
                text: Uni.I18n.translate('deviceCommand.actionMenu.revoke', 'MDC', 'Revoke'),
                action: 'revoke',
                section: this.SECTION_ACTION
            }
        ];
        this.callParent(arguments);
    }
});






