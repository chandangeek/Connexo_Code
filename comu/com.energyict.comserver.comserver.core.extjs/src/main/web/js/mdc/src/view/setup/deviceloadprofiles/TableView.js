Ext.define('Mdc.view.setup.deviceloadprofiles.TableView', {
    extend: 'Uni.view.container.PreviewContainer',
    alias: 'widget.deviceLoadProfilesTableView',
    itemId: 'deviceLoadProfilesTableView',
    requires: [
        'Uni.view.notifications.NoItemsFoundPanel',
        'Mdc.view.setup.deviceloadprofiles.DataGrid',
        'Mdc.view.setup.deviceloadprofiles.DataPreview',
        'Mdc.view.setup.devicechannels.DataPreview'
    ],

    channels: null,

    initComponent: function () {
        var me =this;

        me.grid = {
            xtype:'deviceLoadProfilesDataGrid',
            channels: me.channels
        };

        me.emptyComponent = {
            xtype: 'no-items-found-panel',
            itemId: 'no-load-profile-data',
            title: Uni.I18n.translate('deviceloadprofiles.data.empty.title', 'MDC', 'No readings found'),
            reasons: [
                Uni.I18n.translate('deviceloadprofiles.data.empty.list.item1', 'MDC', 'No readings have been defined yet.')
            ]
        };

        me.previewComponent = {
            xtype: 'deviceLoadProfileChannelDataPreview',
            channels: me.channels
        };

        me.callParent(arguments);
    }
});