/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.devicetype.DeviceTypeActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.device-type-action-menu',
    itemId: 'device-type-action-menu',
    initComponent: function () {
        this.items = [
            {
                text: Uni.I18n.translate('deviceType.addDeviceIcon', 'MDC', 'Add device icon'),
                action: 'addDeviceIcon',
                itemId: 'addDeviceIcon',
                section: this.SECTION_ACTION,
                visible: function () {
                    return Ext.isEmpty(this.record.get('deviceIcon'));
                }
            },
            {
                text: Uni.I18n.translate('deviceType.editDeviceIcon', 'MDC', 'Edit device icon'),
                action: 'editDeviceIcon',
                itemId: 'editDeviceIcon',
                section: this.SECTION_ACTION,
                visible: function () {
                    return !Ext.isEmpty(this.record.get('deviceIcon'));
                }
            },
            {
                text: Uni.I18n.translate('deviceType.removeDeviceIcon', 'MDC', 'Remove device icon'),
                action: 'removeDeviceIcon',
                itemId: 'removeDeviceIcon',
                section: this.SECTION_ACTION,
                visible: function () {
                    return !Ext.isEmpty(this.record.get('deviceIcon'));
                }
            },
            {
                text: Uni.I18n.translate('general.edit', 'MDC', 'Edit'),
                action: 'editDeviceType',
                itemId: 'editDeviceType',
                section: this.SECTION_EDIT
            },
            {
                text: Uni.I18n.translate('deviceLifeCycle.change', 'MDC', 'Change device life cycle'),
                action: 'changeDeviceLifeCycle',
                itemId: 'change-device-life-cycle',
                section: this.SECTION_EDIT
            },
            {
                text: Uni.I18n.translate('general.remove', 'MDC', 'Remove'),
                action: 'deleteDeviceType',
                itemId: 'deleteDeviceType',
                section: this.SECTION_REMOVE
            }
        ];
        this.callParent(arguments);
    },

    listeners: {
        beforeshow: function () {
            var me = this;
            me.items.each(function (item) {
                (item.visible && !item.visible.call(me)) ? item.hide() : item.show();
            });
        }
    }

});
