/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.dataloggerslaves.DataLoggerSlavesActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.dataloggerslaves-action-menu',
    initComponent: function () {
        this.items = [
            {
                itemId: 'mdc-unlink-slave',
                text: Uni.I18n.translate('general.unlink', 'MDC', 'Unlink'),
                privileges: Mdc.privileges.Device.administrateDevice,
                action: 'unlinkSlave',
                section: this.SECTION_ACTION
            }
        ];
        this.callParent(arguments);
    }
});

