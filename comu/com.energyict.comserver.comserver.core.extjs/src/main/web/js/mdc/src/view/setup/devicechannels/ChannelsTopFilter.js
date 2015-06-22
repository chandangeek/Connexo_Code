Ext.define('Mdc.view.setup.devicechannels.ChannelsTopFilter', {
    extend: 'Uni.grid.FilterPanelTop',
    xtype: 'mdc-view-setup-devicechannels-channelstopfilter',

    store: 'Mdc.store.ChannelsOfLoadProfilesOfDevice',

    filters: [
        {
            type: 'text',
            dataIndex: 'channelName',
            emptyText: Uni.I18n.translate('devicechannels.channelstopfilter.channelname.emptytext', 'MDC', 'Channel name')
        },
        {
            type: 'combobox',
            dataIndex: 'loadProfileName',
            emptyText: Uni.I18n.translate('devicechannels.channelstopfilter.loadprofile.emptytext', 'MDC', 'Load profile name'),
            multiSelect: true,
            displayField: 'name',
            valueField: 'name',
            store: 'Mdc.store.LoadProfilesOfDevice'
        }
    ]
});