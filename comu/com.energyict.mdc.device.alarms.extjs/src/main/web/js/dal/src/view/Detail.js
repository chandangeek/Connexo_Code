Ext.define('Dal.view.Detail', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.alarm-detail',
    requires: [
        'Dal.view.DetailTop',
        'Dal.view.Preview',
        'Dal.view.AlarmFilter',
        'Dal.view.EventsAlarmDetailsForm',
        'Isu.view.issues.CommentsList',
        'Dal.view.TimelineList',
        'Bpm.monitorissueprocesses.view.ProcessList',
        'Uni.view.toolbar.PreviousNextNavigation',
        'Dal.store.Alarms'
    ],
    router: null,
    issuesListLink: null,
    initComponent: function () {
        var me = this;

        me.content = [
            {
                xtype: 'container',
                layout: 'hbox',
                items: [
                    {
                        itemId: 'issue-detail-top-title',
                        ui: 'large',
                        flex: 1
                    },
                    {
                        xtype: 'previous-next-navigation-toolbar',
                        margin: '10 0 0 0',
                        itemId: 'alarm-detail-previous-next-navigation-toolbar',
                        store: 'Dal.store.Alarms',
                        router: me.router,
                        routerIdArgument: 'alarmId',
                        itemsName: me.issuesListLink
                    }
                ]
            },
            {
                xtype: 'alarm-detail-top',
                itemId: 'alarm-detail-top',
                router: me.router
            },
            {
                xtype: 'events-alarm-details-form',
                itemId: 'events-alarm-details-form',
                router: me.router,
                store: me.store
            },
            {
                xtype: 'panel',
                ui: 'medium',
                title: Uni.I18n.translate('alarm.context', 'DAL', 'Contextual information'),
                items: [
                    {
                        xtype: 'tabpanel',
                        itemId: 'tab-issue-context',
                        activeTab: 0,
                        items: [
                            {
                                ui: 'medium',
                                title: Uni.I18n.translate('alarm.timeline', 'DAL', 'Timeline'),
                                itemId: 'tab-panel-alarm-timeline',
                                items: [
                                    {
                                        xtype: 'alarm-timeline',
                                        itemId: 'alarm-timeline'
                                    }
                                ]
                            },
                            {
                                ui: 'medium',
                                title: Uni.I18n.translate('alarm.comments', 'DAL', 'Comments'),
                                itemId: 'tab-panel-alarm-comments',
                                items: [
                                    {
                                        xtype: 'issue-comments',
                                        itemId: 'alarm-comments',
                                        noCommentText: Uni.I18n.translate('general.NoAlarmCommentsCreatedYet', 'DAL', 'No comments created yet on this alarm'),
                                        addCommentPrivileges: Dal.privileges.Alarm.comment
                                    }
                                ]
                            },
                            {
                                ui: 'medium',
                                title: Uni.I18n.translate('alarm.processes', 'DAL', 'Processes'),
                                itemId: 'tab-panel-alarm-processes',
                                privileges: Dal.privileges.Alarm.canViewProcesses(),
                                items: [
                                    {
                                        xtype: 'issue-process-list',
                                        itemId: 'alarm-process',
                                        noProcessText: Uni.I18n.translate('processes.alarm.noProcessesStarted', 'DAL', 'No process started yet on this alarm'),
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