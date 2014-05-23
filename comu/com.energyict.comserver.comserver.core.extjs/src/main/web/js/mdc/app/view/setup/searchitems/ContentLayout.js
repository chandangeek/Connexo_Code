Ext.define('Mdc.view.setup.searchitems.ContentLayout', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.contentLayout',
    itemId: 'contentLayout',
    layout: {
        type: 'card',
        align: 'stretch',
        pack: 'center'
    },
    requires: [
        'Mdc.view.setup.searchitems.SearchResults'
    ],

    initComponent: function () {
        var me = this;
        this.items = [
            {
                xtype: 'panel',
                itemId: 'infoPanel',
                layout: {
                    type: 'vbox',
                    align: 'center',
                    pack: 'center'
                },
                items: [
                    {
                        xtype: 'label',
                        html: '<H3>' + Uni.I18n.translate('searchItems.selectText', 'MDC', 'Enter one or more search criteria on the left and click \'Search\'.') + '</H3>'
                    }
                ]
            },
            {
                xtype: 'container',
                itemId: 'resultsPanel',
                layout: {
                    type: 'vbox',
                    align: 'stretch'
                },
                items: []
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
                        html: '<H3>' + Uni.I18n.translate('searchItems.selectText', 'MDC', 'Searching ...') + '</H3>'
                    },
                    {
                        xtype: 'button',
                        ui: 'link',
                        itemId: 'cancelSearching',
                        text: Uni.I18n.translate('searchItems.cancelSearch', 'MDC', 'Cancel')
                    }
                ]
            }
        ]

        this.callParent(arguments);
    }
});