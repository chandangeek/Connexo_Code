Ext.define('Mdc.view.setup.securitysettings.SecuritySettingFloatingPanel', {
    extend: 'Ext.ux.window.Notification',
    alias: 'widget.securitySettingFloatingPanel',
    position: 'tc',
    manager: '#contentPanel',
    slideInDuration: 200,
    slideBackDuration: 200,
    autoCloseDelay: 100000,
    hideDuration: 0,
    slideInAnimation: 'linear',
    slideBackAnimation: 'linear',

    dockedItems: [
        {
            xtype: 'toolbar',
            dock: 'bottom',
            ui: 'footer',
            layout: {
                type: 'hbox'
            },
            items: [
                {
                    xtype: 'button',
                    action: 'delete',
                    name: 'delete',
                    text: 'Remove'
                },
                {
                    xtype: 'button',
                    action: 'cancel',
                    name: 'cancel',
                    text: 'Cancel'
                }
            ]
        }
    ]
});