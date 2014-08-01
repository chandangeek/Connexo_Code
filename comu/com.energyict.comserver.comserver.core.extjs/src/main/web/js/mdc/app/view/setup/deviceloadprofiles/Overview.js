Ext.define('Mdc.view.setup.deviceloadprofiles.Overview', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceLoadProfilesOverview',
    itemId: 'deviceLoadProfilesOverview',
    requires: [
        'Mdc.view.setup.deviceloadprofiles.SubMenuPanel',
        'Mdc.view.setup.deviceloadprofiles.PreviewForm'
    ],

    mRID: null,

    side: {
        xtype: 'deviceLoadProfilesSubMenuPanel'
    },

    initComponent: function () {
        var me = this;

        me.content = {
            xtype: 'deviceLoadProfilesPreviewForm',
            ui: 'large',
            title: Uni.I18n.translate('general.overview', 'MDC', 'Overview'),
            mRID: me.mRID
        };

        me.callParent(arguments);
    }
});

