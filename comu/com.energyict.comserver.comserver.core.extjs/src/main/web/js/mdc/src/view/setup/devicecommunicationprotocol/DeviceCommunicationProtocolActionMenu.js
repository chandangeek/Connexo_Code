/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.devicecommunicationprotocol.DeviceCommunicationProtocolActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.device-communication-protocol-action-menu',
    itemId: 'device-communication-protocol-action-menu',
    initComponent: function () {
        this.items = [
            {
                text: Uni.I18n.translate('general.edit', 'MDC', 'Edit'),
                itemId: 'editProtocol',
                action: 'editProtocol',
                section: this.SECTION_EDIT
            }
        ];
        this.callParent(arguments);
    }
});
