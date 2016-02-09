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
            //privileges: Apr.privileges.AppServer.admin,
            action: 'cancel',
            visible: function() {
                //you can access this.record here! -> check if the cancel option is possible. Same for other options.
                return false;
            }
        },
        {
            itemId: 'resume-scs',
            text: Uni.I18n.translate('general.resume', 'SCS', 'Resume'),
            //privileges: Apr.privileges.AppServer.admin,
            action: 'resume',
            visible: function() {
                return true;
            }
        },
        {
            itemId: 'pause-scs',
            text: Uni.I18n.translate('general.pause', 'SCS', 'Pause'),
            //privileges: Apr.privileges.AppServer.admin,
            action: 'pause',
            visible: function() {
                return false;
            }
        },
        {
            itemId: 'retry-scs',
            text: Uni.I18n.translate('general.retry', 'SCS', 'Retry'),
            //privileges: Apr.privileges.AppServer.admin,
            action: 'retry',
            visible: function() {
                return true;
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