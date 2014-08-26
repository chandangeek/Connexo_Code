Ext.define('Mdc.view.setup.searchitems.bulk.Step4', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.searchitems-bulk-step4',
    bodyCls: 'isu-bulk-wizard-no-border',
    name: 'confirmPage',
    title: Uni.I18n.translate('searchItems.bulk.step4title', 'MDC', 'Bulk action - step 4 of 5: Confirmation'),
    ui: 'large',
    tbar: {
        xtype: 'panel',
        ui: 'medium',
        style: {
            padding: '0 0 0 3px'
        },
        title: '',
        itemId: 'searchitemsbulkactiontitle'
    },
    showMessage: function (message) {
        var widget = {
            html: '<h3>' + message.title + '</h3><br>' + message.body
        };

        this.removeAll();
        this.add(widget)
    }
});