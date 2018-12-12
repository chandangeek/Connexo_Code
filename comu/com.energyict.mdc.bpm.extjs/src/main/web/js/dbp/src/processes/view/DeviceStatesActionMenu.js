/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dbp.processes.view.DeviceStatesActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.dbp-device-states-action-menu',
    initComponent: function() {
        this.items = [
            {
                itemId: 'remove-device-state',
                text: Uni.I18n.translate('general.remove', 'DBP', 'Remove'),
                privileges: Dbp.privileges.DeviceProcesses.administrateProcesses,
                action: 'removeDeviceState',
                section: this.SECTION_REMOVE
            }
        ];
        this.callParent(arguments);
    }
});