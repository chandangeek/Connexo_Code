Ext.define('Fwc.view.firmware.ActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.firmware-action-menu',
    plain: true,
    border: false,
    itemId: 'firmware-action-menu',
    shadow: false,
    items: [
        {
            text: Uni.I18n.translate('general.edit', 'FWC', 'Edit'),
            action: 'editFirmware',
            itemId: 'editFirmware'
        },
        {
            text: Uni.I18n.translate('firmware.final', 'FWC', 'Set as final'),
            action: 'setFinal',
            itemId: 'setFinal',
            visible: function () {
                return this.record.getAssociatedData().firmwareStatus
                    && this.record.getAssociatedData().firmwareStatus.id === 'test';
            }
        },
        {
            text: Uni.I18n.translate('firmware.deprecate', 'FWC', 'Deprecate'),
            action: 'deprecate',
            itemId: 'deprecate'
        }
    ],

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
