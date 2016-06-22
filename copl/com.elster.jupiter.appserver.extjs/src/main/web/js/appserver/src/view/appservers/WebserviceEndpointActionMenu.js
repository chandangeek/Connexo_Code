Ext.define('Apr.view.appservers.WebserviceEndpointActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.apr-webservices-action-menu',
    plain: true,
    border: false,
    shadow: false,
    items: [
        {
            itemId: 'remove-webservice',
            text: Uni.I18n.translate('general.remove', 'APR', 'Remove'),
            //action: 'removeImportService'
        }
    ]
});