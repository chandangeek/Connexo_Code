/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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
        this.getCenterContainer().down('#searchContentFilter').hide();

    }
});
