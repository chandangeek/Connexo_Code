/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.deviceloadprofiles.TableView', {
    extend: 'Uni.view.container.PreviewContainer',
    alias: 'widget.deviceLoadProfilesTableView',
    itemId: 'deviceLoadProfilesTableView',
    requires: [
        'Uni.util.FormEmptyMessage',
        'Mdc.view.setup.deviceloadprofiles.DataGrid',
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
            xtype: 'uni-form-empty-message',
            itemId: 'no-load-profile-data',
            text: Uni.I18n.translate('deviceloadprofiles.data.empty', 'MDC', 'No readings have been defined yet.')
        };

        me.previewComponent = {
            xtype: 'deviceLoadProfileChannelDataPreview',
            router: me.router,
            channels: me.channels
        };

        me.callParent(arguments);
    }
});