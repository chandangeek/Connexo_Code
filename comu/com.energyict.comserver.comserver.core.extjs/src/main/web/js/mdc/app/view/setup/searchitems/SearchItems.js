Ext.define('Mdc.view.setup.searchitems.SearchItems', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.searchItems',
    itemId: 'searchItems',
    //id: 'search-items-id',
    cls: 'filter-form',
    requires: [
        'Mdc.view.setup.searchitems.SideFilter',
        'Mdc.view.setup.searchitems.ContentFilter',
        'Mdc.view.setup.searchitems.ContentLayout'

    ],

    content: [
        {
            title: Uni.I18n.translate('searchItems.filter.title', 'MDC', 'Search'),
            ui: 'medium'
        },
        {
            xtype: 'search-content-filter'
        },
        {
            xtype: 'contentLayout'
        }
    ],
    side: [
        {
            xtype: 'search-side-filter'
        }
    ],

    initComponent: function () {
        this.callParent(arguments);

        this.down('#contentLayout').getLayout().setActiveItem(0);
    }
});
