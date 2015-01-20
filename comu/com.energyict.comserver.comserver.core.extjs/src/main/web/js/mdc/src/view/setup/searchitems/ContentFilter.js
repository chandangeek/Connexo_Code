Ext.define('Mdc.view.setup.searchitems.ContentFilter', {
    extend: 'Ext.container.Container',
    requires: [
        'Ext.form.Label',
        'Mdc.view.setup.searchitems.SortMenu',
        'Mdc.model.ItemSort'
    ],
    alias: "widget.search-content-filter",
    itemId: 'searchContentFilter',
    layout: {
        type: 'vbox',
        align: 'stretch'
    },

    items: [
        {
            title: Uni.I18n.translate('searchItems.filter.criteria', 'MDC', 'Criteria'),
            xtype: 'filter-toolbar',
            itemId: 'filteritemid',
            name: 'filter',
            emptyText: 'None'
        },
        { xtype: 'menuseparator' },

// Sort
        {
            xtype: 'filter-toolbar',
            title: Uni.I18n.translate('searchItems.filter.sort', 'MDC', 'Sort'),
            name: 'sortitemspanel',
            itemId: 'sortitemid',
            emptyText: 'None',
            tools: [
                {
                    xtype: 'button',
                    action: 'addSort',
                    text: 'Add sort',
                    menu: {
                        xtype: 'items-sort-menu',
                        name: 'addsortitemmenu'
                    }
                }
            ]
        }
    ]
});