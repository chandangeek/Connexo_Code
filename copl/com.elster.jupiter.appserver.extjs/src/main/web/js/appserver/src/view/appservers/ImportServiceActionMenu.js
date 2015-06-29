Ext.define('Apr.view.appservers.ImportServiceActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.apr-import-services-action-menu',
    plain: true,
    border: false,
    shadow: false,
    items: [
        {
            itemId: 'remove-import-service',
            text: Uni.I18n.translate('general.remove', 'UNI', 'Remove'),
            action: 'removeImportService'
        }
    ]
});