Ext.define('Wss.view.WebservicesPreviewContainer', {
    extend: 'Uni.view.container.PreviewContainer',
    alias: 'widget.webservices-preview-container',

    requires: [
        'Uni.util.FormEmptyMessage',
        'Wss.view.Grid',
        'Wss.view.Preview'
    ],

    emptyComponent: {
        xtype: 'uni-form-empty-message',
        itemId: 'no-webservices',
        text: Uni.I18n.translate('webservices.mpty', 'WSS', 'No webservice endpoints have been defined yet.')
    },

    router: null,

    initComponent: function () {
        var me = this;
        me.grid = {
            xtype: 'webservices-grid',
            itemId: 'grd-webservices',
            router: me.router
        };

        me.previewComponent = {
            xtype: 'webservices-preview',
            itemId: 'pnl-webservices-preview',
        };

        me.callParent(arguments);
    }
});