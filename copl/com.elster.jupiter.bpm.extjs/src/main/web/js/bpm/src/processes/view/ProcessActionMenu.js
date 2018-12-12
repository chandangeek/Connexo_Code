/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Bpm.processes.view.ProcessActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.bpm-process-action-menu',
    initComponent: function () {
        this.items =
            [
                {
                    itemId: 'menu-activate-process',
                    text: Uni.I18n.translate('bpm.menu.activateProcess', 'BPM', 'Activate'),
                    action: 'activateProcess',
                    privileges: Bpm.privileges.BpmManagement.administrateProcesses,
                    section: this.SECTION_ACTION
                },
                {
                    itemId: 'menu-deactivate-process',
                    text: Uni.I18n.translate('bpm.menu.deactivateProcess', 'BPM', 'Deactivate'),
                    action: 'deactivateProcess',
                    privileges: Bpm.privileges.BpmManagement.administrateProcesses,
                    section: this.SECTION_ACTION
                },
                {
                    itemId: 'menu-edit-process',
                    text: Uni.I18n.translate('bpm.menu.editProcess', 'BPM', 'Edit process'),
                    action: 'editProcess',
                    privileges: Bpm.privileges.BpmManagement.administrateProcesses,
                    section: this.SECTION_EDIT
                }
            ];
        this.callParent(arguments);
    },

    listeners: {
        beforeshow: function (menu) {
            if (menu.record.get('active') === 'ACTIVE') {
                menu.down('#menu-activate-process').hide();
                menu.down('#menu-edit-process').show();
                menu.down('#menu-deactivate-process').show();
            } else {
                menu.down('#menu-edit-process').hide();
                menu.down('#menu-deactivate-process').hide();
                menu.down('#menu-activate-process').show();
            }
        }
    }
});


