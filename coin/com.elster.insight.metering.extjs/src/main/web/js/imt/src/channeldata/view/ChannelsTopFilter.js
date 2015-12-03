Ext.define('Imt.channeldata.view.ChannelsTopFilter', {
    extend: 'Uni.grid.FilterPanelTop',
    xtype: 'channelsTopFilter',

    store: 'Imt.channeldata.store.Channels',

    filters: [
        {
            type: 'text',
            dataIndex: 'channelName',
            emptyText: Uni.I18n.translate('devicechannels.channelstopfilter.channelname.emptytext', 'IMT', 'Channel name')
        },
        {
            type: 'combobox',
            dataIndex: 'loadProfileName',
            emptyText: Uni.I18n.translate('devicechannels.channelstopfilter.loadprofile.emptytext', 'IMT', 'Load profile name'),
            multiSelect: true,
            displayField: 'name',
            valueField: 'name',
            store: 'Imt.store.LoadProfilesOfDevice'
        }
    ]
});