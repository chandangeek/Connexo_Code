Ext.define('Mdc.view.setup.searchitems.SortMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.items-sort-menu',
    plain: true,
    border: false,
    shadow: false,
    items: [
        {
            text: Uni.I18n.translate('searchItems.mrid', 'MDC', 'MRID'),
            action: 'sortbymrid'
        },
        {
            text: Uni.I18n.translate('searchItems.serialNumber', 'MDC', 'Serial number'),
            action: 'sortbysn'
        }
    ]
});
