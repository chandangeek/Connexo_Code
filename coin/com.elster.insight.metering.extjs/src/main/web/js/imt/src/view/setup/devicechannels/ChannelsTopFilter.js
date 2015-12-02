Ext.define('Imt.view.setup.devicechannels.ChannelsTopFilter', {
    extend: 'Uni.grid.FilterPanelTop',
    xtype: 'mdc-view-setup-devicechannels-channelstopfilter',

    store: 'Imt.store.ChannelsOfLoadProfilesOfDevice',

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