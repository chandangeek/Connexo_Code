/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.keyfunctiontypes.view.KeyFunctionTypesActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.key-function-types-action-menu',
    initComponent: function () {
        this.items = [
            {
                text: Uni.I18n.translate('general.edit', 'MDC', 'Edit'),
                privileges: Mdc.privileges.DeviceType.canAdministrate(),
                action: 'edit',
                section: this.SECTION_EDIT
            },
            {
                text: Uni.I18n.translate('general.remove', 'MDC', 'Remove'),
                privileges: Mdc.privileges.DeviceType.canAdministrate(),
                action: 'remove',
                section: this.SECTION_REMOVE
            }
        ];
        this.callParent(arguments);
    }
});