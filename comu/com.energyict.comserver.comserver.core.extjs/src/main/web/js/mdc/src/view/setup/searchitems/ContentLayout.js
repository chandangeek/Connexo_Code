/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.searchitems.ContentLayout', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.contentLayout',
    itemId: 'contentLayout',
    layout: {
        type: 'card'
    },
    requires: [
        'Mdc.view.setup.searchitems.SearchResults'
    ],

    initComponent: function () {
        var me = this;
        me.items = [
            {
                xtype: 'label',
                layout: {
                    type: 'hbox',
                    align: 'left'
                },
                html: '<H3>' + Uni.I18n.translate('searchItems.selectText', 'MDC', "Enter one or more search criteria on the left and click 'Search'.") + '</H3>'
            },
            {
                xtype: 'preview-container',
                itemId: 'resultsPanel',
                grid: {
                    xtype: 'searchResults',
                    store: 'Mdc.store.Devices'
                },
                emptyComponent: {
                    xtype: 'no-items-found-panel',
                    itemId: 'no-devices',
                    title: Uni.I18n.translate('searchItems.empty.title', 'MDC', 'No devices found'),
                    reasons: [
                        Uni.I18n.translate('searchItems.empty.list.item1', 'MDC', 'No devices have been defined yet.'),
                        Uni.I18n.translate('searchItems.empty.list.item2', 'MDC', 'No devices comply with the filter.')
                    ]
                }
            },
            {
                xtype: 'container',
                itemId: 'loadingPanel',
                layout: {
                    type: 'vbox',
                    align: 'center',
                    pack: 'center'
                },
                items: [
                    {
                        xtype: 'label',
                        html: '<H3>' + Uni.I18n.translate('searchItems.searching', 'MDC', 'Searching ...') + '</H3>'
                    },
                    {
                        xtype: 'button',
                        ui: 'link',
                        itemId: 'cancelSearching',
                        text: Uni.I18n.translate('searchItems.cancelSearch', 'MDC', 'Cancel')
                    }
                ]
            }
        ];

        this.callParent(arguments);
    }
});