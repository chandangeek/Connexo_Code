Ext.define('Cal.view.ActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.tou-action-menu',
    initComponent: function() {
        this.items = [
            {
                itemId: 'view-preview-cal',
                text: Uni.I18n.translate('general.viewPreview', 'CAL', 'View preview'),
                action: 'viewpreview',
                section: this.SECTION_VIEW
            },
            {
                itemId: 'remove-preview-cal',
                text: Uni.I18n.translate('general.remove', 'CAL', 'Remove'),
                privileges: Cal.privileges.Calendar.admin,
                action: 'remove',
                visible: function () {
                    return !this.record.get('inUse');
                },
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
                    item.visible.call(me) ? item.show() : item.hide();
                }
            })
        }
    }
});