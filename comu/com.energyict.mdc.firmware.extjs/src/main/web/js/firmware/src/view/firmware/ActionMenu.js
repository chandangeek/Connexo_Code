/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Fwc.view.firmware.ActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.firmware-action-menu',
    itemId: 'firmware-action-menu',
    initComponent: function() {
        this.items = [
            {
                text: Uni.I18n.translate('general.edit', 'FWC', 'Edit'),
                action: 'editFirmware',
                itemId: 'editFirmware',
                section: this.SECTION_EDIT
            },
            {
                text: Uni.I18n.translate('firmware.final', 'FWC', 'Set as final'),
                action: 'setFinal',
                itemId: 'setFinal',
                visible: function () {
                    return this.record.getAssociatedData().firmwareStatus
                        && this.record.getAssociatedData().firmwareStatus.id === 'test';
                },
                section: this.SECTION_ACTION
            },
            {
                text: Uni.I18n.translate('general.deprecate', 'FWC', 'Deprecate'),
                action: 'deprecate',
                itemId: 'deprecate',
                section: this.SECTION_ACTION
            }
        ];
        this.callParent(arguments);
    },

    listeners: {
        beforeshow: function () {
            var me = this;
            if (me.record.getAssociatedData().firmwareStatus
             && me.record.getAssociatedData().firmwareStatus.id === 'deprecated') {
                // do not show the menu
                return false;
            } else {
                me.items.each(function (item) {
                    (item.visible && !item.visible.call(me)) ? item.hide() : item.show();
                });
            }
        }
    }
});
