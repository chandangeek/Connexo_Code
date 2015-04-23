Ext.define('Fwc.devicefirmware.view.ActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.device-firmware-action-menu',
    itemId: 'device-firmware-action-menu',
    shadow: false,
    items: [
        {
            text: 'Upload firmware',
            itemId: 'uploadFirmware',
            action: 'uploadFirmware'
        },
        {
            text: 'Upload firmware and activate immediately',
            itemId: 'uploadActivateNow',
            action: 'uploadActivateNow'
        },
        {
            text: 'Upload firmware with activation date',
            itemId: 'uploadActivateInDate',
            action: 'uploadActivateInDate'
        }
    ]
});
