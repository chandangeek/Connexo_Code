Ext.define('Mdc.view.setup.devicelogbooks.Overview', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceLogbookOverview',
    itemId: 'deviceLogbookOverview',
    requires: [
        'Mdc.view.setup.devicelogbooks.SubMenuPanel',
        'Mdc.view.setup.devicelogbooks.PreviewForm'
    ],

    mRID: null,
    router: null,

    content: {
        xtype: 'deviceLogbooksPreviewForm',
        ui: 'large',
        title: Uni.I18n.translate('general.overview', 'MDC', 'Overview')
    },

    initComponent: function () {
        var me = this;

        me.side = {
            xtype: 'deviceLogbookSubMenuPanel',
            router: me.router
        };

        me.callParent(arguments);
    }
});

