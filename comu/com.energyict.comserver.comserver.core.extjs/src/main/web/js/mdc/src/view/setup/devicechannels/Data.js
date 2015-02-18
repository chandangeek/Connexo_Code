Ext.define('Mdc.view.setup.devicechannels.Data', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceLoadProfileChannelData',
    itemId: 'deviceLoadProfileChannelData',
    requires: [
        'Mdc.view.setup.devicechannels.TableView',
        'Mdc.view.setup.devicechannels.GraphView',
        'Mdc.view.setup.devicechannels.SideFilter',
        'Uni.view.toolbar.PagingTop'
    ],

    router: null,
    channel: null,
    viewType: null,

    initComponent: function () {
        var me = this;
        me.content = {
            ui: 'large',
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
                    xtype: 'filter-top-panel',
                    itemId: 'deviceloadprofileschanneldatafilterpanel',
                    emptyText: Uni.I18n.translate('general.none', 'MDC', 'None')
                }
            ]
        };

        switch (me.viewType) {
            case 'table':
                me.content.items.push({
                    xtype: 'deviceLoadProfileChannelTableView',
                    channel: me.channel,
                    router: me.router
                });
                break;
            case 'graph':
                me.content.items.push({
                    xtype: 'deviceLoadProfileChannelGraphView'
                });
                break;
        }

        me.callParent(arguments);
    }
});

