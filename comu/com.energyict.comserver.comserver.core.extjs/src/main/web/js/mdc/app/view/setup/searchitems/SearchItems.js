Ext.define('Mdc.view.setup.searchitems.SearchItems', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.searchItems',
    itemId: 'searchItems',
    //id: 'search-items-id',
    cls: 'filter-form',
    requires: [
        'Mdc.view.setup.searchitems.SideFilter',
        'Mdc.view.setup.searchitems.ContentFilter',
        'Mdc.view.setup.searchitems.ContentLayout',
        'Mdc.model.DeviceType'
    ],

    content: [
        {
            xtype: 'panel',
            title: Uni.I18n.translate('searchItems.filter.title', 'MDC', 'Search'),
            ui: 'large',
            items: [
                {
                    xtype: 'search-content-filter'
                },
                {
                    xtype: 'contentLayout'
                }
            ]
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

        Ext.getStore('DeviceTypes').on('load', function(store) {
            store.insert(0, Ext.create('Mdc.model.DeviceType', {
                id: -1,
                name: '&nbsp;'
            }));
        });
    }
});
