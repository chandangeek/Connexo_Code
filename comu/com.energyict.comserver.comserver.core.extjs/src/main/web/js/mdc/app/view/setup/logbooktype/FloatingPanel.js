Ext.define('Mdc.view.setup.logbooktype.FloatingPanel', {
    extend: 'Ext.ux.window.Notification',
    alias: 'widget.logbook-floating-panel',
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
                    action: 'cancel',
                    name: 'close',
                    text: 'Close'
                },
                {
                    xtype: 'button',
                    action: 'delete',
                    name: 'retry',
                    text: 'Retry'
                },
                {
                    xtype: 'button',
                    action: 'delete',
                    name: 'delete',
                    text: 'Delete'
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
