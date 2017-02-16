/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.timeofuse.view.ActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.tou-devicetype-action-menu',
    initComponent: function () {
        this.items = [
            {
                itemId: 'view-preview-tou',
                text: Uni.I18n.translate('timeofuse.viewPreview', 'MDC', 'View preview'),
                privileges: Mdc.privileges.DeviceType.view,
                action: 'viewpreview',
                visible: function () {
                    return !this.record.get('ghost');
                },
                section: this.SECTION_VIEW
            },
            {
                itemId: 'remove-tou',
                text: Uni.I18n.translate('general.remove', 'MDC', 'Remove'),
                privileges: Mdc.privileges.DeviceType.admin,
                action: 'remove',
                section: this.SECTION_REMOVE
            }
        ];
        this.callParent(arguments);
    },

    listeners: {
        beforeshow: function () {
            var me = this;
            me.items.each(function (item) {
                if (item.visible === undefined) {
                    item.show();
                } else {
                    item.visible.call(me) ? item.show() : item.hide(); //hier nog privileges in de check?
                }
            })
        }
    }
});