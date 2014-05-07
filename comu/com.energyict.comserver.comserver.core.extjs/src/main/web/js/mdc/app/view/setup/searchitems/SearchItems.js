Ext.define('Mdc.view.setup.searchitems.SearchItems', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.searchItems',
    itemId: 'searchItems',
    id: 'search-items-id',
    cls: 'filter-form',
    requires: [
//        'Uni.component.filter.view.Filter',
//        'Uni.component.sort.model.Sort',
        'Mdc.view.setup.searchitems.SideFilter',
        'Mdc.view.setup.searchitems.ContentFilter',
        'Mdc.view.setup.searchitems.ContentLayout'

    ],

    content: [
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
