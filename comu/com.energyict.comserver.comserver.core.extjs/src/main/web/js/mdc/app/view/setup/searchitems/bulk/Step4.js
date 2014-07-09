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
        title: '',
        itemId: 'searchitemsbulkactiontitle'
    },
    showMessage: function (message) {
        this.removeAll();
        widget = Ext.widget('container', {
            cls: 'isu-bulk-assign-confirmation-request-panel',
            html: message
        });
        this.add(widget)
    }
});