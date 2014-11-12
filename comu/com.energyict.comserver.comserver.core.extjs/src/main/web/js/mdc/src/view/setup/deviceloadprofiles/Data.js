Ext.define('Mdc.view.setup.deviceloadprofiles.Data', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.deviceLoadProfilesData',
    itemId: 'deviceLoadProfilesData',
    requires: [
        'Mdc.view.setup.deviceloadprofiles.SubMenuPanel',
        'Mdc.view.setup.deviceloadprofiles.TableView',
        'Mdc.view.setup.deviceloadprofiles.GraphView',
        'Mdc.view.setup.deviceloadprofiles.SideFilter'
    ],

    router: null,
    channels: null,

    initComponent: function () {
        var me = this;

        me.content = {
            ui: 'large',
            title: Uni.I18n.translate('deviceloadprofiles.loadProfileData', 'MDC', 'Load profile data'),
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
                    xtype: 'filter-top-panel',
                    itemId: 'deviceloadprofilesdatafilterpanel',
                    emptyText: Uni.I18n.translate('general.none', 'MDC', 'None')
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

        me.side = {
            xtype: 'panel',
            ui: 'medium',
            items: [
                {
                    xtype: 'deviceLoadProfilesSubMenuPanel',
                    router: me.router
                },
                {
                    xtype: 'deviceLoadProfileDataSideFilter'
                }
            ]
        };

        me.callParent(arguments);
    }
});

