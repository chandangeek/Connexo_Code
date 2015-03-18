Ext.define('Fwc.view.firmware.ActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.firmware-action-menu',
    plain: true,
    border: false,
    itemId: 'firmware-action-menu',
    shadow: false,
    items: [
        {
            text: Uni.I18n.translate('general.edit', 'MDC', 'Edit'),
            action: 'editFirmware',
            itemId: 'editFirmware'
        },
        {
            text: Uni.I18n.translate('firmware.final', 'MDC', 'Set as final'),
            action: 'setFinal',
            itemId: 'setFinal',
            visible: function () {
                return this.record.get('status') === 'test';
            }
        },
        {
            text: Uni.I18n.translate('firmware.deprecate', 'MDC', 'Deprecate'),
            action: 'deprecate',
            itemId: 'deprecate'
        }
    ],

    listeners: {
        beforeshow: function () {
            var me = this;
            if (me.record.get('status') === 'deprecated') {
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
