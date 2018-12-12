/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dbp.processes.view.PrivilegesActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.dbp-privileges-action-menu',
    initComponent: function() {
        this.items = [
            {
                itemId: 'remove-device-state',
                text: Uni.I18n.translate('general.remove', 'DBP', 'Remove'),
                privileges: Dbp.privileges.DeviceProcesses.administrateProcesses,
                action: 'removePrivileges',
                section: this.SECTION_REMOVE
            }
        ];
        this.callParent(arguments);
    }
});