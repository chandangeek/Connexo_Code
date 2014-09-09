Ext.define('Mdc.view.setup.deviceloadprofiles.Preview', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.deviceLoadProfilesPreview',
    itemId: 'deviceLoadProfilesPreview',
    requires: [
        'Mdc.view.setup.deviceloadprofiles.PreviewForm',
        'Mdc.view.setup.deviceloadprofiles.ActionMenu'
    ],
    layout: 'fit',
    frame: true,
    router: null,

    mRID: null,

    tools: [
        {
            xtype: 'button',
            text: Uni.I18n.translate('general.actions', 'MDC', 'Actions'),
            itemId: 'actionButton',
            iconCls: 'x-uni-action-iconD',
            menu: {
                xtype: 'deviceLoadProfilesActionMenu'
            }
        }
    ],

    initComponent: function () {
        var me = this;

        me.items = {
            xtype: 'deviceLoadProfilesPreviewForm',
            mRID: me.mRID,
            router: me.router
        };

        me.callParent(arguments);
    }
});
