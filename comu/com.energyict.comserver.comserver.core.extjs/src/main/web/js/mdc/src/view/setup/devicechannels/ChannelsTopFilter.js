/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.devicechannels.ChannelsTopFilter', {
    extend: 'Uni.grid.FilterPanelTop',
    xtype: 'mdc-view-setup-devicechannels-channelstopfilter',

    requires: [
        'Mdc.store.LoadProfilesOfDevice'
    ],

    store: 'Mdc.store.ChannelsOfLoadProfilesOfDevice',
    initComponent: function () {
        var me = this;
        me.filters = [
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
            },
            {
                type: 'text',
                hidden: me.hasSapAttributes ? false : true,
                dataIndex: 'logicalRegisterNumber',
                emptyText: Uni.I18n.translate('devicechannels.channelstopfilter.lrn.emptytext', 'MDC', 'Logical register number')
            },
            {
                type: 'text',
                hidden: me.hasSapAttributes ? false : true,
                dataIndex: 'profileId',
                emptyText: Uni.I18n.translate('devicechannels.channelstopfilter.profileId.emptytext', 'MDC', 'Profile id')
            },
        ]

        me.callParent(arguments);
    }
});