Ext.define('Mdc.view.setup.securitysettings.SecuritySettingFloatingPanel', {
    extend: 'Ext.window.MessageBox',
    alias: 'widget.securitySettingFloatingPanel',
    position: 'tc',
    manager: '#contentPanel',

    buttons: [
        {
            xtype: 'button',
            action: 'delete',
            name: 'delete',
            text: 'Remove',
            ui: 'delete'
        },
        {
            xtype: 'button',
            action: 'cancel',
            name: 'cancel',
            ui: 'link',
            text: 'Cancel'
        }
    ]
});