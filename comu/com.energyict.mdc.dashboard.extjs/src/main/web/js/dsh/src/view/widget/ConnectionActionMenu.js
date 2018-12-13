/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dsh.view.widget.ConnectionActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.connection-action-menu',
    communicationViewMode: false,
    initComponent: function() {
        this.items = [
            {
                text: Uni.I18n.translate('general.runNow', 'DSH', 'Run now'),
                privileges : Mdc.privileges.Device.operateDeviceCommunication,
                action: 'run',
                itemId: 'dsh-connection-action-menu-run-now',
                section: this.SECTION_ACTION
            },
            {
                text: Uni.I18n.translate('general.viewHistory', 'DSH', 'View history'),
                action: 'viewHistory',
                itemId: 'dsh-connection-action-menu-view-history',
                section: this.SECTION_VIEW
            },
            {
                text: Uni.I18n.translate('general.viewLog', 'DSH', 'View log'),
                action: 'viewLog',
                itemId: 'dsh-connection-action-menu-view-log',
                section: this.SECTION_VIEW
            },
            {
                text: Uni.I18n.translate('general.viewCommunicationTasks', 'DSH', 'View communication tasks'),
                action: 'viewCommunicationTasks',
                itemId: 'dsh-connection-action-menu-view-communicationTasks',
                section: this.SECTION_VIEW
            }
        ];
        this.callParent(arguments);
    },

    listeners: {
        beforeshow: function (menu) {
            if(menu.communicationViewMode) {
                menu.down('#dsh-connection-action-menu-run-now').hide();
                menu.down('#dsh-connection-action-menu-view-history').hide();
                if(menu.record.get('connectionTask').comSessionId !== 0) {
                    menu.down('#dsh-connection-action-menu-view-log').show();
                } else {
                    menu.down('#dsh-connection-action-menu-view-log').hide();
                }
            } else {
                if (menu && menu.record) {
                    var viewLogMenuItem = menu.down('menuitem[action=viewLog]');
                    if (menu.record.get('comSessionId') !== 0 && menu.down('menuitem[action=viewLog]') !== null) {
                        viewLogMenuItem.show();
                    } else {
                        viewLogMenuItem.hide();
                    }
                }
            }
        }
    }
});

