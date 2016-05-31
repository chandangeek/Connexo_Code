Ext.define('Cal.view.ActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.tou-action-menu',
    plain: true,
    border: false,
    shadow: false,
    items: [
        {
            itemId: 'view-preview-cal',
            text: Uni.I18n.translate('general.viewPreview', 'CAL', 'View preview'),
            action: 'viewpreview'
        },
        {
            itemId: 'remove-preview-cal',
            text: Uni.I18n.translate('general.remove', 'CAL', 'Remove'),
            privileges: Cal.privileges.Calendar.admin,
            action: 'remove',
            visible: function () {
                return !this.record.get('inUse');
            }
        }
    ],
    listeners: {
        beforeshow: function () {
            var me = this;
            me.items.each(function (item) {
                if (item.visible === undefined) {
                    item.show();
                } else {
                    item.visible.call(me) ? item.show() : item.hide();
                }
            })
        }
    }
});