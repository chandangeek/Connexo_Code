Ext.define('Apr.view.appservers.SortingToolbar', {
    extend: 'Uni.view.panel.FilterToolbar',
    requires: [
        'Uni.view.button.SortItemButton',
        'Apr.view.appservers.SortingMenu'
    ],
    alias: 'widget.appservers-sorting-toolbar',
    title: Uni.I18n.translate('general.sort', 'APR', 'Sort'),
    emptyText: Uni.I18n.translate('general.none', 'APR', 'None'),
    showClearButton: false,
    tools: [

    ]
});
