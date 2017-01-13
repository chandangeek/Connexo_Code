Ext.define('Dal.view.Overview', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.overview-of-alarms',
    requires: [
        'Dal.view.AlarmFilter',
        'Dal.view.Section',
        'Dal.view.NoAlarmsFoundPanel'
    ],
    router: null,
    initComponent: function () {
        var me = this;

        me.content = [
            {
                title: Uni.I18n.translate('workspace.alarmsOverview', 'DAL', 'Alarms overview'),
                ui: 'large',
                itemId: 'overview-of-alarms-main-panel',
                items: [
                    {
                        xtype: 'view-alarms-filter',
                        itemId: 'overview-of-alarms-alarmfilter',
                        store: new Ext.data.ArrayStore(),
                        isOverviewFilter: true
                    },
                    {
                        xtype: 'no-alarms-found-panel',
                        itemId: 'overview-no-alarms-found-panel',
                        hidden: true
                    },
                    {
                        itemId: 'sections-panel',
                        hidden: true,
                        items: [
                            {
                                layout: {
                                    type: 'hbox',
                                    align: 'stretch'
                                },
                                defaults: {
                                    xtype: 'overview-of-alarms-section',
                                    ui: 'tile',
                                    flex: 1
                                },
                                items: [
                                    {
                                        title: Uni.I18n.translate('workspace.perStatus', 'DAL', 'Per status'),
                                        itemId: 'status',
                                        margin: '20 10 0 0'
                                    },
                                    {
                                        title: Uni.I18n.translate('workspace.perWorkgroupAssignee', 'DAL', 'Per workgroup'),
                                        itemId: 'workgroupAssignee',
                                        margin: '20 0 0 10'
                                    }

                                ]
                            },
                            {
                                layout: {
                                    type: 'hbox'
                                },
                                defaults: {
                                    xtype: 'overview-of-alarms-section',
                                    ui: 'tile',
                                    flex: 1
                                },
                                items: [
                                    {
                                        title: Uni.I18n.translate('workspace.perUserAssignee', 'DAL', 'Per user'),
                                        itemId: 'userAssignee',
                                        margin: '20 10 0 0'
                                    },
                                    {
                                        title: Uni.I18n.translate('workspace.perReason', 'DAL', 'Per reason'),
                                        itemId: 'reason',
                                        margin: '20 0 0 10'
                                    }
                                ]
                            }
                        ]
                    }
                ]
            }
        ];

        me.callParent(arguments);
    }
});