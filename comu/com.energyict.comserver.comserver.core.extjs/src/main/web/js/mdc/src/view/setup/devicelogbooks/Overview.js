Ext.define('Mdc.view.setup.devicelogbooks.Overview', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceLogbookOverview',
    itemId: 'deviceLogbookOverview',
    requires: [
        'Mdc.view.setup.devicelogbooks.SubMenuPanel',
        'Mdc.view.setup.devicelogbooks.PreviewForm'
    ],
    toggleId: null,
    router: null,
    device: null,
    content: {
        xtype: 'deviceLogbooksPreviewForm',
        ui: 'large',
        title: Uni.I18n.translate('general.overview', 'MDC', 'Overview')
    },

    initComponent: function () {
        var me = this;

        me.side = {
            xtype: 'panel',
            title: Uni.I18n.translate('deviceregisterconfiguration.devices', 'MDC', 'Devices'),
            ui: 'medium',
            items: [
                {
                    xtype: 'deviceMenu',
                    itemId: 'stepsMenu',
                    device: me.device,
                    toggleId: me.toggleId
                }
            ]
        };

        me.callParent(arguments);
    }
});

