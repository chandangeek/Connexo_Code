/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mdc.registereddevices.view.ActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.registered-devices-kpi-action-menu',

    initComponent: function () {
        this.items = [
            {
                text: Uni.I18n.translate('general.edi', 'MDC', 'Edit'),
                action: 'edit',
                section: this.SECTION_EDIT
            },
            {
                text: Uni.I18n.translate('general.remove', 'MDC', 'Remove'),
                action: 'remove',
                section: this.SECTION_REMOVE
            }
        ];
        this.callParent(arguments);
    }
});
