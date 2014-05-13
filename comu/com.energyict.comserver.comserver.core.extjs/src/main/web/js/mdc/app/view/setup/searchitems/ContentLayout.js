Ext.define('Mdc.view.setup.searchitems.ContentLayout', {
    extend: 'Ext.panel.Panel',
    padding: '10 10 10 10',
    alias: 'widget.contentLayout',
    itemId: 'contentLayout',
    layout: {
        type: 'card',
        align: 'stretch',
        pack: 'center'
    },

    initComponent: function () {
        var me = this;
        this.items = [
            {
                xtype: 'panel',
                layout: {
                    type: 'vbox',
                    align: 'middle',
                    pack: 'center'
                },
                items: [
                    {
                        xtype: 'component',
                        html: '<h5>' + Uni.I18n.translate('searchItems.selectText', 'MDC', 'Enter one or more search criteria on the left and click \'Search\'.') + '</h5>'
                    }
                ]
            },
            {
                xtype: 'form',
                layout: {
                    type: 'vbox',
                    align: 'stretch'
                },
                items: [
                    {
                        xtype: 'component',
                        html: '<h5>' + Uni.I18n.translate('registerGroup.selectRegisterGroup', 'MDC', 'Select a register group to see its details') + '</h5>'
                    }
                ]
            }
        ]

    this.callParent(arguments);
  }
});