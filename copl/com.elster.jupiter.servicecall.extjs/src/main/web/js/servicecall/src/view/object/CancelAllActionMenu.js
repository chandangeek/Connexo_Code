Ext.define('Scs.view.object.CancelAllActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.cancel-all-action-menu',
    plain: true,
    border: false,
    shadow: false,
    record: null,
    items: [
        {
            itemId: 'cancel-all-scs',
            text: Uni.I18n.translate('general.cancelAll', 'SCS', 'Cancel all'),
            privileges: Scs.privileges.ServiceCall.admin,
            action: 'cancel-all'
        }
    ],
    listeners: {
        beforeshow: function() {
            var me = this;
            me.items.each(function(item){
                if (item.visible === undefined) {
                    item.show();
                } else {
                    item.visible.call(me) ?  item.show() : item.hide();
                }
            })
        }
    }
});