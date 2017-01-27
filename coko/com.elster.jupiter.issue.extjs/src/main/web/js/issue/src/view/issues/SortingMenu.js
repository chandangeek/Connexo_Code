Ext.define('Isu.view.issues.SortingMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.issues-sorting-menu',
    shadow: false,
    border: false,
    plain: true,
    items: [
        {
            itemId: 'issues-sorting-menu-item-by-priority',
            text: Uni.I18n.translate('general.title.priority', 'ISU', 'Priority'),
            action: 'priority'
        }
    ]
});