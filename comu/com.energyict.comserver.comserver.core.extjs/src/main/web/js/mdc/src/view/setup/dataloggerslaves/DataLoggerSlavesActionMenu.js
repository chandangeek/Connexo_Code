Ext.define('Mdc.view.setup.dataloggerslaves.DataLoggerSlavesActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.dataloggerslaves-action-menu',
    plain: true,
    border: false,
    shadow: false,
    items: [
        {
            itemId: 'mdc-unlink-slave',
            text: Uni.I18n.translate('general.unlink', 'MDC', 'Unlink'),
            privileges: Mdc.privileges.Device.administrateDevice,
            action: 'unlinkSlave'
        }
    ]
});

