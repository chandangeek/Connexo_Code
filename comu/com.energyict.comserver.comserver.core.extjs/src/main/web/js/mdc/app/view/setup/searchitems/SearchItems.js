Ext.define('Mdc.view.setup.searchitems.SearchItems', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.searchItems',
    itemId: 'searchItems',
    cls: 'filter-form',
    requires: [
        'Mdc.view.setup.searchitems.SideFilter',
        'Mdc.view.setup.searchitems.ContentFilter',
        'Mdc.view.setup.searchitems.ContentLayout',
        'Mdc.model.DeviceType',
        'Mdc.model.DeviceConfiguration'
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
            xtype: 'contentLayout',
            flex:1
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

        Ext.getStore('DeviceTypes').on('load', function loadDeviceTypes(store) {
            store.insert(0, Ext.create('Mdc.model.DeviceType', {
                id: -1,
                name: '&nbsp;'
            }));
            this.removeListener('load', loadDeviceTypes);
        });
    }
});