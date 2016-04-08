Ext.define('Mdc.timeofuse.view.ActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.tou-action-menu',
    plain: true,
    border: false,
    shadow: false,
    items: [
        {
            itemId: 'view-preview-tou',
            text: Uni.I18n.translate('timeofuse.viewPreview', 'MDC', 'View preview'),
            //privileges: Scs.privileges.ServiceCall.admin,
            action: 'viewpreview',
            //visible: function() {
            //    return this.record.get('canCancel');
            //}
        },
        {
            itemId: 'remove-tou',
            text: Uni.I18n.translate('general.remove', 'MDC', 'Remove'),
            //privileges: Scs.privileges.ServiceCall.admin,
            action: 'remove',
            //visible: function() {
            //    return this.record.get('canCancel');
            //}
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