Ext.define('Mdc.timeofuse.view.SpecificationsActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.tou-spec-action-menu',
    plain: true,
    border: false,
    shadow: false,
    items: [
        {
            itemId: 'edit-tou-specifications',
            text: Uni.I18n.translate('general.edit', 'MDC', 'Edit'),
            //privileges: Scs.privileges.ServiceCall.admin,
            action: 'editspecifications'
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