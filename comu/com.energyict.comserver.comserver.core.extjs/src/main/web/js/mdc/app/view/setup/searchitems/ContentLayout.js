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
                    align: 'middle',
                    pack: 'center'
                },
                items: [
                    {
                        xtype: 'component',
                        html: '<H5>' + Uni.I18n.translate('searchItems.selectText', 'MDC', 'Enter one or more search criteria on the left and click \'Search\'.') + '</H5>'
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
                    align: 'stretch'
                },
                items: [
                    {
                        xtype: 'component',
                        html: '<H5>' + Uni.I18n.translate('searchItems.selectText', 'MDC', 'Searching ...') + '</H5>'
                    }
                ]
            }
        ]

    this.callParent(arguments);
  }
});