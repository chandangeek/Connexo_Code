Ext.define('Dsh.view.OperatorDashboard', {
    extend: 'Ext.panel.Panel',
    requires: [
        'Mdc.privileges.Device',
        'Mdc.privileges.DeviceGroup',
        'Isu.privileges.Issue',
        'Dal.privileges.Alarm',
        'Bpm.privileges.BpmManagement',
        'Dsh.view.widget.HeaderSection',
        'Dsh.view.widget.Summary',
        'Dsh.view.widget.CommunicationServers',
        'Dsh.view.widget.QuickLinks',
        'Dsh.view.widget.ReadOutsOverTime',
        'Dsh.view.widget.OpenDataCollectionIssues',
        'Dsh.view.widget.Overview',
        'Dsh.view.widget.Breakdown',
        'Dsh.view.widget.DeviceGroupFilter',
        'Dsh.view.widget.FavoriteDeviceGroups',
        'Dsh.view.widget.FlaggedDevices',
        'Dsh.view.MyFavoriteDeviceGroups',
        'Uni.view.widget.WorkList'
    ],
    alias: 'widget.operator-dashboard',
    itemId: 'operator-dashboard',
    autoScroll: true,
    layout: {
        type: 'vbox',
        align: 'stretch'
    },
    style: {
        'padding-left': '20px'
    },
    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'panel',
                ui: 'large',
                title:  me.router.getRoute().title,
                tools: [
                    {
                        xtype: 'toolbar',
                        style: {
                            marginRight: '20px'
                        },
                        items: [
                            '->',
                            {
                                xtype: 'component',
                                itemId: 'last-updated-field',
                                width: 150,
                                style: {
                                    'font': 'normal 13px/17px Lato',
                                    'color': '#686868'
                                }
                            },
                            {
                                xtype: 'button',
                                itemId: 'refresh-btn',
                                style: {
                                    'background-color': '#71adc7'
                                },
                                text: Uni.I18n.translate('overview.widget.headerSection.refreshBtnTxt', 'DSH', 'Refresh'),
                                iconCls: 'icon-spinner11'
                            }
                        ]
                    }
                ],
                layout: {
                    type: 'hbox',
                    align: 'stretch'
                },
                defaults: {
                    flex: 1
                },
                items: []
            },
            {
                xtype: 'toolbar',
                margin: '50 0 0 0',
                items: {
                    xtype: 'device-group-filter',
                    privileges: Mdc.privileges.Device.administrateOrOperateDeviceCommunication,
                    router: me.router
                }
            },
            {
                xtype: 'panel',
                layout: {
                    type: 'hbox',
                    align: 'stretch'
                },
                items: [
                    {
                        xtype: 'panel',
                        layout: {
                            type: 'vbox',
                            align: 'stretch'
                        },
                        height: 500,
                        defaults: {
                            flex: 1
                        },
                        items: [],
                        flex: 1
                    },
                    {
                        xtype: 'panel',
                        layout: {
                            type: 'vbox',
                            align: 'stretch'
                        },
                        defaults: {
                            flex: 1
                        },
                        items: [],
                        width: 350
                    }
                ]
            }
        ];

        if (Isu.privileges.Issue.canViewAdminDevice() ||
            Dal.privileges.Alarm.canViewAdmimAlarm() ||
            Bpm.privileges.BpmManagement.canView()) {

            var items = [];
            if (Isu.privileges.Issue.canViewAdminDevice()) {

                var item = {
                    type: 'issues',
                    name: Uni.I18n.translate('widget.myWorkList.issues', 'DSH', 'Issues'),
                    topLabel: Uni.I18n.translate('widget.myWorkList.issues.topLabel', 'DSH', 'Top {0} most urgent issues'),
                    topZeroLabel: Uni.I18n.translate('widget.myWorkList.issues.topZeroLabel', 'DSH', 'No open issues assigned to me'),
                    url: '/api/isu/topissues/issues',
                    itemRoute: 'workspace/issues/view',
                    routeArguments: [{name: 'issueId', property: 'id'}],
                    queryParams: [{name: 'issueType', value: 'datacollection'}],
                    assignedToMeLink: me.router.getRoute('workspace/issues').buildUrl({}, {myopenissues: true, issueType: 'datacollection'}),
                    myWorkgroupsLink: me.router.getRoute('workspace/issues').buildUrl({}, {myworkgroupissues: true, issueType: 'datacollection'}),
                    userProperty: 'actualOwner',
                    titleProperty: 'title',
                    workgroupProperty: 'workgroup',
                    tooltipProperties: [
                        {
                            name: 'title',
                            label: Uni.I18n.translate('widget.myWorkList.property.name', 'DSH', 'Name')
                        },
                        {
                            name: 'dueDate',
                            label: Uni.I18n.translate('widget.myWorkList.property.dueDate', 'DSH', 'Due date'),
                            type: 'datetime'
                        }
                    ]
                };
                items.push(item);
            }

            if (Dal.privileges.Alarm.canViewAdmimAlarm()) {
                var item = {
                    type: 'alarms',
                    name: Uni.I18n.translate('widget.myWorkList.alarms', 'DSH', 'Alarms'),
                    topLabel: Uni.I18n.translate('widget.myWorkList.alarms.topLabel', 'DSH', 'Top {0} most urgent alarms'),
                    topZeroLabel: Uni.I18n.translate('widget.myWorkList.alarms.topZeroLabel', 'DSH', 'No open alarms assigned to me'),
                    url: '/api/isu/topissues/alarms',
                    itemRoute: 'workspace/alarms/view',
                    routeArguments: [{name: 'alarmId', property: 'id'}],
                    assignedToMeLink: me.router.getRoute('workspace/alarms').buildUrl({}, {myopenalarms: true}),
                    myWorkgroupsLink: me.router.getRoute('workspace/alarms').buildUrl({}, {myworkgroupalarms: true}),
                    userProperty: 'userAssignee',
                    titleProperty: 'title',
                    workgroupProperty: 'workgroup',
                    tooltipProperties: [
                        {
                            name: 'title',
                            label: Uni.I18n.translate('widget.myWorkList.property.name', 'DSH', 'Name')
                        },
                        {
                            name: 'dueDate',
                            label: Uni.I18n.translate('widget.myWorkList.property.dueDate', 'DSH', 'Due date'),
                            type: 'datetime'
                        }
                    ]
                };
                items.push(item);

            }

            if (Bpm.privileges.BpmManagement.canView()) {
                var item = {
                    type: 'userTasks',
                    name: Uni.I18n.translate('widget.myWorkList.userTasks', 'DSH', 'User tasks'),
                    topLabel: Uni.I18n.translate('widget.myWorkList.topLabel', 'DSH', 'Top {0} most urgent user tasks'),
                    topZeroLabel: Uni.I18n.translate('widget.myWorkList.topZeroLabel', 'DSH', 'No open user tasks assigned to me'),
                    url: '/api/bpm/runtime/toptasks/',
                    itemRoute: 'workspace/tasks/task',
                    routeArguments: [{name: 'taskId', property: 'id'}],
                    assignedToMeLink: me.router.getRoute('workspace/tasks').buildUrl({}, {param: 'myopentasks'}),
                    myWorkgroupsLink: me.router.getRoute('workspace/tasks').buildUrl({}, {param: 'myworkgroups'}),
                    userProperty: 'actualOwner',
                    titleProperty: 'name',
                    workgroupProperty: 'workgroup',
                    tooltipProperties: [
                        {
                            name: 'name',
                            label: Uni.I18n.translate('widget.myWorkList.property.name', 'DSH', 'Name')
                        },
                        {
                            name: 'dueDate',
                            label: Uni.I18n.translate('widget.myWorkList.property.dueDate', 'DSH', 'Due date'),
                            type: 'datetime'
                        },
                        {
                            name: 'priority',
                            label: Uni.I18n.translate('widget.myWorkList.property.priority', 'DSH', 'Priority'),
                            type: 'priority'
                        },
                        {
                            name: 'processInstancesId',
                            label: Uni.I18n.translate('widget.myWorkList.property.processInstancesId', 'DSH', 'Process Id')
                        },
                        {
                            name: 'processName',
                            label: Uni.I18n.translate('widget.myWorkList.property.processName', 'DSH', 'Process name')
                        }
                    ]
                };
                items.push(item);

            }

            me.items[0].items.push(
                {
                    xtype: 'work-list',
                    itemId: 'my-work-list',
                    router: me.router,

                    configuration: {
                        title: Uni.I18n.translate('widget.myWorkList.title', 'DSH', 'My worklist'),
                        assignedToMeLabel: Uni.I18n.translate('widget.myWorkList.assignedToMeLabel', 'DSH', 'Assigned to me ({0})'),
                        myWorkgroupsLabel: Uni.I18n.translate('widget.myWorkList.myWorkgroupsLabel', 'DSH', 'In my workgroup(s) ({0})'),
                        items: items
                    }
                }
            );
        }


        if(Mdc.privileges.Device.canAdministrateDeviceData() ||
            Mdc.privileges.Device.canView() ||
            Mdc.privileges.Device.canAdministrateOrOperateDeviceCommunication()) {
            me.items[0].items.push(
                {
                    xtype: 'flagged-devices',
                    itemId: 'flagged-devices',
                    router: me.router
                });
        }
        //if(Uni.Auth.hasAnyPrivilege(['privilege.administrate.deviceGroup','privilege.administrate.deviceOfEnumeratedGroup','privilege.view.deviceGroupDetail'])) {
            me.items[0].items.push(
                {
                    xtype: 'favorite-device-groups',
                    itemId: 'favorite-device-groups'
                });
        //}
        if (Mdc.privileges.Device.canAdministrateOrOperateDeviceCommunication()) {
            me.items[2].items[0].items.push(
                {
                    xtype: 'summary',
                    itemId: 'connection-summary',
                    router: me.router,
                    parent: 'connections',
                    buttonAlign: 'left',
                    buttons: [{
                        text: Uni.I18n.translate('dashboard.widget.connections.link', 'DSH', 'View connections overview'),
                        itemId: 'lnk-connections-overview',
                        ui: 'link',
                        href: typeof me.router.getRoute('workspace/connections') !== 'undefined'
                            ? me.router.getRoute('workspace/connections').buildUrl(null, me.router.queryParams) : ''
                    }]
                },
                {
                    xtype: 'summary',
                    itemId: 'communication-summary',
                    parent: 'communications',
                    router: me.router,
                    buttonAlign: 'left',
                    buttons: [{
                        text: Uni.I18n.translate('dashboard.widget.communications.link', 'DSH', 'View communications overview'),
                        itemId: 'lnk-communications-overview',
                        ui: 'link',
                        href: typeof me.router.getRoute('workspace/communications') !== 'undefined'
                            ? me.router.getRoute('workspace/communications').buildUrl(null, me.router.queryParams) : ''
                    }]
                });
        }

        me.items[2].items[1].items.push(
            {
                xtype: 'communication-servers',
                privileges: Mdc.privileges.Device.administrateOrOperateDeviceCommunication,
                itemId: 'communication-servers',
                router: me.router
            }
        );
        this.callParent(arguments);
    }
});