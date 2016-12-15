Ext.define('Imt.usagepointmanagement.view.bulk.Step4', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.usagepoints-bulk-step4',
    bodyCls: 'isu-bulk-wizard-no-border',
    name: 'confirmPage',
    layout: 'hbox',
    title: Uni.I18n.translate('usagepoints.bulk.step4title', 'IMT', 'Step 4: Confirmation'),
    ui: 'large',
    tbar: {
        xtype: 'panel',
        ui: 'medium',
        style: {
            padding: '0 0 0 3px'
        },
        title: '',
        itemId: 'usagepointsbulkactiontitle'
    },

    showMessage: function (message) {
        var widget = {
            html: '<h3>' + Ext.String.htmlEncode(message.title) + '</h3><br>' + Ext.String.htmlEncode(message.body)
        };
        Ext.suspendLayouts();
        this.removeAll();
        this.add(widget);
        Ext.resumeLayouts();
    }

});