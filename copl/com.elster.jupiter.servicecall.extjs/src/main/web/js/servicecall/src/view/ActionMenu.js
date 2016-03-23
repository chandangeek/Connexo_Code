Ext.define('Scs.view.ActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.scs-action-menu',
    plain: true,
    border: false,
    shadow: false,
    record: null,
    items: [
        {
            itemId: 'cancel-scs',
            text: Uni.I18n.translate('general.cancel', 'SCS', 'Cancel'),
            privileges: Scs.privileges.ServiceCall.admin,
            action: 'cancel',
            visible: function() {
                return this.record.get('canCancel');
            }
        }
    ],
    listeners: {
        beforeshow: function() {
            var me = this;
            me.items.each(function(item){
                if (item.visible === undefined) {
                    item.show();
                } else {
                    item.visible.call(me) ?  item.show() : item.hide(); //hier nog privileges in de check?
                }
            })
        }
    }
});