Ext.define('Imt.dashboard.view.OperatorDashboard', {
    extend: 'Ext.panel.Panel',
    requires: [
        'Uni.view.widget.WorkList',
        'Imt.dashboard.view.widget.FlaggedUsagePoints',
        'Imt.dashboard.view.widget.FlaggedUsagePointGroups'
    ],
    alias: 'widget.operator-dashboard',
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
                title: me.router.getRoute().title,
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
                                margins: '0 15 0 0'
                            },
                            {
                                xtype: 'button',
                                itemId: 'refresh-btn',
                                text: Uni.I18n.translate('general.refresh', 'IMT', 'Refresh'),
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
            }

        ];

        if (Bpm.privileges.BpmManagement.canView() || Imt.privileges.UsagePoint.canFlag() || Imt.privileges.UsagePointGroup.canFlag()) {
            var item = {
                xtype: 'container',
                layout: {
                    type: 'hbox',
                    align: 'stretch'
                },
                items: []
            };

            if (Bpm.privileges.BpmManagement.canView()) {
                item.items.push({
                    xtype: 'work-list',
                    itemId: 'my-work-list',
                    router: me.router,
                    flex: .33,
                    configuration: {
                        title: Uni.I18n.translate('widget.myWorkList.title', 'IMT', 'My worklist'),
                        assignedToMeLabel: Uni.I18n.translate('widget.myWorkList.assignedToMeLabel', 'IMT', 'Assigned to me ({0})'),
                        myWorkgroupsLabel: Uni.I18n.translate('widget.myWorkList.myWorkgroupsLabel', 'IMT', 'In my workgroup(s) ({0})'),
                        items: [
                            {
                                type: 'userTasks',
                                name: Uni.I18n.translate('widget.myWorkList.userTasks', 'IMT', 'User tasks'),
                                topLabel: Uni.I18n.translate('widget.myWorkList.topLabel', 'IMT', 'Top {0} most urgent user tasks'),
                                topZeroLabel: Uni.I18n.translate('widget.myWorkList.topZeroLabel', 'IMT', 'No open user tasks assigned to me'),
                                url: '/api/bpm/runtime/toptasks/',
                                itemRoute: 'workspace/tasks/task',
                                routeArguments: [{name: 'taskId', property: 'id'}],
                                assignedToMeLink: me.router.getRoute('workspace/tasks').buildUrl({}, {param: 'myopentasks'}),
                                myWorkgroupsLink: me.router.getRoute('workspace/tasks').buildUrl({}, {param: 'myworkgroups'}),
                                userProperty: 'actualOwner',
                                workgroupProperty: 'workgroup',
                                titleProperty: 'name',
                                tooltipProperties: [
                                    {
                                        name: 'name',
                                        label: Uni.I18n.translate('widget.myWorkList.property.name', 'IMT', 'Name')
                                    },
                                    {
                                        name: 'dueDate',
                                        label: Uni.I18n.translate('widget.myWorkList.property.dueDate', 'IMT', 'Due date'),
                                        type: 'datetime'
                                    },
                                    {
                                        name: 'priority',
                                        label: Uni.I18n.translate('widget.myWorkList.property.priority', 'IMT', 'Priority'),
                                        type: 'priority'
                                    },
                                    {
                                        name: 'processInstancesId',
                                        label: Uni.I18n.translate('widget.myWorkList.property.processInstancesId', 'IMT', 'Process ID')
                                    },
                                    {
                                        name: 'processName',
                                        label: Uni.I18n.translate('widget.myWorkList.property.processName', 'IMT', 'Process name')
                                    }
                                ]
                            }
                        ]
                    }
                });
            }
            ;

            if (Imt.privileges.UsagePoint.canFlag()) {
                item.items.push({
                    xtype: 'flagged-usage-points',
                    itemId: 'flagged-usage-points',
                    router: me.router,
                    flex: .33
                });
            }
            ;

            if (Imt.privileges.UsagePointGroup.canFlag()) {
                item.items.push({
                    xtype: 'flagged-usage-point-groups',
                    itemId: 'flagged-usage-point-groups',
                    router: me.router,
                    flex: .33
                });
            }
            ;

            for (var i = item.items.length; i < 3; i++) {
                item.items.push({
                    xtype: 'container',
                    flex: .33
                });
            }
            me.items[0].items.push(item);
        }

        me.callParent(arguments);
    }
});