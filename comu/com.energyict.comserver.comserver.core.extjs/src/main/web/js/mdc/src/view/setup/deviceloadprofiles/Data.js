Ext.define('Mdc.view.setup.deviceloadprofiles.Data', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceLoadProfilesData',
    itemId: 'deviceLoadProfilesData',
    requires: [
        'Mdc.view.setup.deviceloadprofiles.TableView',
        'Mdc.view.setup.deviceloadprofiles.GraphView',
        'Mdc.view.setup.deviceloadprofiles.LoadProfileTopFilter'
    ],

    router: null,
    channels: null,

    initComponent: function () {
        var me = this;

        me.content = {
            ui: 'large',
            tools: [
                {
                    xtype: 'button',
                    itemId: 'deviceLoadProfilesTableViewBtn',
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
                    itemId: 'deviceLoadProfilesGraphViewBtn',
                    text: Uni.I18n.translate('deviceloadprofiles.graphView', 'MDC', 'Graph view'),
                    action: 'showGraphView',
                    disabled: true,
                    ui: 'link'
                }
            ],
            items: [
                {
                    xtype: 'mdc-loadprofiles-topfilter',
                    itemId: 'deviceloadprofilesdatafilterpanel'
                },
                {
                    items: [
                        {
                            xtype: 'deviceLoadProfilesTableView',
                            channels: me.channels
                        },
                        {
                            xtype: 'deviceLoadProfilesGraphView'
                        }
                    ]
                }
            ]
        };

        me.callParent(arguments);
    }
});

