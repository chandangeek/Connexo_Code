Ext.define('Mdc.timeofuse.view.ActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.tou-devicetype-action-menu',
    plain: true,
    border: false,
    shadow: false,
    items: [
        {
            itemId: 'view-preview-tou',
            text: Uni.I18n.translate('timeofuse.viewPreview', 'MDC', 'View preview'),
            privileges: Mdc.privileges.DeviceType.view,
            action: 'viewpreview',
            visible: function () {
                return !this.record.get('ghost');
            }
        },
        {
            itemId: 'remove-tou',
            text: Uni.I18n.translate('general.remove', 'MDC', 'Remove'),
            privileges: Mdc.privileges.DeviceType.admin,
            action: 'remove'
        }

    ],
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