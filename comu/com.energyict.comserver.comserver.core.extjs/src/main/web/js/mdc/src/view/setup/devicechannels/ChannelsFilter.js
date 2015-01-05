Ext.define('Mdc.view.setup.devicechannels.ChannelsFilter', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.device-channels-filter',
    itemId: 'device-channels-filter',
    ui: 'medium',
    width: 288,
    title: Uni.I18n.translate('connection.widget.sideFilter.title', 'DSH', 'Filter'),
    items: [
        {
            xtype: 'nested-form',
            itemId: 'filter-form',
            ui: 'filter',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            items: [
                {
                    xtype: 'textfield',
                    itemId: 'channel-name',
                    name: 'channelName',
                    labelAlign: 'top',
                    fieldLabel: Uni.I18n.translate('device.channels.channelname', 'DSH', 'Channel name')
                },
                {
                    xtype: 'combobox',
                    itemId: 'load-profile-name',
                    name: 'loadProfileName',
                    displayField: 'name',
                    valueField: 'name',
                    store: 'Mdc.store.LoadProfilesOfDevice',
                    labelAlign: 'top',
                    fieldLabel: Uni.I18n.translate('device.channels.loadprofilename', 'DSH', 'Load profile name')
                }
            ],
            dockedItems: [
                {
                    xtype: 'toolbar',
                    dock: 'bottom',
                    items: [
                        {
                            text: Uni.I18n.translate('connection.widget.sideFilter.apply', 'DSH', 'Apply'),
                            ui: 'action',
                            action: 'applyfilter'
                        },
                        {
                            text: Uni.I18n.translate('connection.widget.sideFilter.clearAll', 'DSH', 'Clear all'),
                            action: 'clearfilter'
                        }
                    ]
                }
            ]
        }
    ]
});
