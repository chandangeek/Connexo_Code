Ext.define('Scs.view.ActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.scs-action-menu',
    plain: true,
    border: false,
    shadow: false,
    items: [
        {
            itemId: 'cancel-scs',
            text: Uni.I18n.translate('general.cancel', 'SCS', 'Cancel'),
            //privileges: Apr.privileges.AppServer.admin,
            action: 'cancel'
        },
        {
            itemId: 'resume-scs',
            text: Uni.I18n.translate('general.resume', 'SCS', 'Resume'),
            //privileges: Apr.privileges.AppServer.admin,
            action: 'resume'
        },
        {
            itemId: 'pause-scs',
            text: Uni.I18n.translate('general.pause', 'SCS', 'Pause'),
            //privileges: Apr.privileges.AppServer.admin,
            action: 'pause'
        },
        {
            itemId: 'retry-scs',
            text: Uni.I18n.translate('general.retry', 'SCS', 'Retry'),
            //privileges: Apr.privileges.AppServer.admin,
            action: 'retry'
        }
    ]
});