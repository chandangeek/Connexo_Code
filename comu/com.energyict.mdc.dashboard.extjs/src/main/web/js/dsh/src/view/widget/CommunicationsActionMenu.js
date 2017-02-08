/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dsh.view.widget.CommunicationsActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.communications-action-menu',
    initComponent: function () {
        this.items = [
            {
                text: Uni.I18n.translate('general.run', 'DSH', 'Run'),
                action: 'run',
                itemId: 'dsh-communication-actions-menu-run',
                section: this.SECTION_ACTION
            },
            {
                text: Uni.I18n.translate('general.runNow', 'DSH', 'Run now'),
                privileges: Mdc.privileges.Device.operateDeviceCommunication,
                action: 'runNow',
                itemId: 'dsh-communication-actions-menu-run-now',
                section: this.SECTION_ACTION
            },
            {
                text: Uni.I18n.translate('general.viewLog', 'DSH', 'View log'),
                action: 'viewLog',
                itemId: 'dsh-communication-actions-menu-view-log',
                section: this.SECTION_VIEW
            }
        ];
        this.callParent(arguments);
    },

    listeners: {
        beforeshow: function (menu) {
            var record = menu.record;
            menu.down('#dsh-communication-actions-menu-view-log').setVisible(record.get('sessionId') !== 0);
            if (record.get('connectionTask').connectionStrategy && record.get('connectionTask').connectionStrategy.id) {
                if (record.get('connectionTask').connectionStrategy.id === 'MINIMIZE_CONNECTIONS') {
                    menu.down('#dsh-communication-actions-menu-run').show();
                } else {
                    menu.down('#dsh-communication-actions-menu-run').hide();
                }
                menu.down('#dsh-communication-actions-menu-run-now').show();
            } else {
                menu.down('#dsh-communication-actions-menu-run').hide();
                menu.down('#dsh-communication-actions-menu-run-now').hide();
            }
        }
    }
});

