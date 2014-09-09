Ext.define('Mdc.view.setup.deviceloadprofilechannels.Overview', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceLoadProfileChannelOverview',
    itemId: 'deviceLoadProfileChannelOverview',
    requires: [
        'Mdc.view.setup.deviceloadprofilechannels.SubMenuPanel',
        'Mdc.view.setup.deviceloadprofilechannels.PreviewForm'
    ],

    router: null,

    content: {
        xtype: 'deviceLoadProfileChannelsPreviewForm',
        ui: 'large',
        title: Uni.I18n.translate('general.overview', 'MDC', 'Overview')
    },

    initComponent: function () {
        var me = this;

        me.side = {
            xtype: 'deviceLoadProfileChannelSubMenuPanel',
            router: me.router
        };

        me.callParent(arguments);
    }
});