Ext.define('Mdc.view.setup.deviceloadprofilechannels.Data', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceLoadProfileChannelData',
    itemId: 'deviceLoadProfileChannelData',
    requires: [
        'Mdc.view.setup.deviceloadprofilechannels.SubMenuPanel',
        'Mdc.view.setup.deviceloadprofilechannels.TableView',
        'Mdc.view.setup.deviceloadprofilechannels.GraphView',
        'Mdc.view.setup.deviceloadprofilechannels.TopFilter',
        'Mdc.view.setup.deviceloadprofilechannels.SideFilter'
    ],

    router: null,
    channel: null,

    initComponent: function () {
        var me = this;
        me.content = {
            ui: 'large',
            title: Uni.I18n.translate('deviceloadprofiles.channels.channelData', 'MDC', 'Channel data'),
            tools: [
                {
                    xtype: 'button',
                    itemId: 'deviceLoadProfileChannelTableViewBtn',
                    text: Uni.I18n.translate('deviceloadprofiles.tableView', 'MDC', 'Table view'),
                    action: 'showTableView',
                    ui: 'link'
                },
                {
                    xtype: 'tbtext',
                    text: '|'
                },
                {
                    xtype: 'button',
                    itemId: 'deviceLoadProfileChannelGraphViewBtn',
                    text: Uni.I18n.translate('deviceloadprofiles.graphView', 'MDC', 'Graph view'),
                    action: 'showGraphView',
                    disabled: true,
                    ui: 'link'
                }
            ],
            items: [
                {
                    xtype: 'deviceLoadProfileChannelDataTopFilter'
                },
                {
                    tbar: {
                        xtype: 'toolbar',
                        items: [
                            {
                                xtype: 'container',
                                itemId: 'readingsCount',
                                hidden: true,
                                flex: 1
                            }
                        ]
                    },
                    items: [

                        {
                            xtype: 'deviceLoadProfileChannelTableView',
                            channel: me.channel,
                            hidden: true
                        },
                        {
                            xtype: 'deviceLoadProfileChannelGraphView'
                        }
                    ]
                }
            ]
        };

        me.side = {
            xtype: 'panel',
            ui: 'medium',
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            items: [
                {
                    xtype: 'deviceLoadProfileChannelSubMenuPanel',
                    router: me.router
                },
                {
                    xtype: 'deviceLoadProfileChannelDataSideFilter'
                }
            ]
        };

        me.callParent(arguments);
    }
});

