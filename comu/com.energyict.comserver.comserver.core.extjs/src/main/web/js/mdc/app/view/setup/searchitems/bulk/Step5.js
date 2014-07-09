Ext.define('Mdc.view.setup.searchitems.bulk.Step5', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.searchitems-bulk-step5',
    title: Uni.I18n.translate('searchItems.bulk.step5title', 'MDC', 'Bulk action - step 5 of 5: Status'),
    ui: 'large',
    name: 'statusPage',
    defaults: {
        margin: '0 0 8 0'
    },
    tbar: {
        xtype: 'panel',
        ui: 'medium',
        title: '',
        itemId: 'searchitemsbulkactiontitle'
    },
    initComponent: function () {
        this.callParent(arguments);
    }
});