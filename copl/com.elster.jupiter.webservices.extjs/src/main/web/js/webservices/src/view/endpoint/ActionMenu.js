/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Wss.view.endpoint.ActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.webservices-endpoint-action-menu',
    initComponent: function() {
        this.items = [
            {
                itemId: 'endpoint-occurrence-view-payload',
                text: Uni.I18n.translate('general.viewPayload', 'WSS', 'View payload'),
                action: 'view-payload',
                // privileges: Wss.privileges.Webservices.admin,
                section: this.SECTION_EDIT
            },
        ];
        this.callParent(arguments);
    },
});