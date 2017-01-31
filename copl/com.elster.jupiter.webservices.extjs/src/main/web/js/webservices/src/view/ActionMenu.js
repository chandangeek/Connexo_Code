/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Wss.view.ActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.webservices-action-menu',
    initComponent: function() {
        this.items = [
            {
                itemId: 'activate-webservice',
                action: 'activate',
                privileges: Wss.privileges.Webservices.admin,
                section: this.SECTION_ACTION
            },
            {
                itemId: 'edit-webservice',
                text: Uni.I18n.translate('general.edit', 'WSS', 'Edit'),
                action: 'edit',
                privileges: Wss.privileges.Webservices.admin,
                section: this.SECTION_EDIT
            },
            {
                itemId: 'remove-webservice',
                text: Uni.I18n.translate('general.remove', 'WSS', 'Remove'),
                action: 'remove',
                privileges: Wss.privileges.Webservices.admin,
                section: this.SECTION_REMOVE
            }
        ];
        this.callParent(arguments);
    },
    listeners: {
        beforeshow: function () {
            var me = this;
            me.down('#activate-webservice').setText(me.record.get('active') ? Uni.I18n.translate('general.deactivate', 'WSS', 'Deactivate')
                : Uni.I18n.translate('general.activate', 'WSS', 'Activate'));
        }
    }
});