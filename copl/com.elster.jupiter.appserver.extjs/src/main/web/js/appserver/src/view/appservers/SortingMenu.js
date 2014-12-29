Ext.define('Apr.view.appservers.SortingMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.appservers-sorting-menu',
    shadow: false,
    border: false,
    plain: true,
    items: [
        {
            itemId: 'appservers-sorting-menu-item',
            text: Uni.I18n.translate('general.name', 'UNI', 'Name'),
            action: 'sortByName'
        }
    ]
});