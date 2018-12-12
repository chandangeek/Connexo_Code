/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dsh.view.widget.connection.CommunicationActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.communication-action-menu',
    initComponent: function () {
        this.items = [
            {
                text: Uni.I18n.translate('general.viewLog', 'DSH', 'View log'),
                action: 'viewLog',
                itemId: 'dsh-communication-action-menu-view-log',
                section: this.SECTION_VIEW
            }
        ];
        this.callParent(arguments);
    },

    listeners: {
        beforeshow: function (menu) {
            var record = menu.record;
            menu.down('#dsh-communication-action-menu-view-log').setVisible(record.get('sessionId') !== 0);
        }
    }
});

