/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dbp.processes.view.ProcessActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.dbp-process-action-menu',
    initComponent: function() {
        this.items = [
            {
                itemId: 'menu-edit-process',
                text: Uni.I18n.translate('dbp.menu.editProcess', 'DBP', 'Edit process'),
                action: 'editProcess',
                privileges: Dbp.privileges.DeviceProcesses.administrateProcesses,
                section: this.SECTION_EDIT
            },
            {
                itemId: 'menu-activate-process',
                text: Uni.I18n.translate('dbp.menu.activateProcess', 'DBP', 'Activate'),
                action: 'activateProcess',
                privileges: Dbp.privileges.DeviceProcesses.administrateProcesses,
                section: this.SECTION_ACTION
            },
            {
                itemId: 'menu-deactivate-process',
                text: Uni.I18n.translate('dbp.menu.deactivateProcess', 'DBP', 'Deactivate'),
                action: 'deactivateProcess',
                privileges: Dbp.privileges.DeviceProcesses.administrateProcesses,
                section: this.SECTION_ACTION
            }
        ];
        this.callParent(arguments);
    }
});


