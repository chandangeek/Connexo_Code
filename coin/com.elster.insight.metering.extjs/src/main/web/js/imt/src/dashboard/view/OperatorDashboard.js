Ext.define('Imt.dashboard.view.OperatorDashboard', {
    extend: 'Ext.panel.Panel',
    requires: [
        'Uni.view.widget.WorkList'
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
                                text: Uni.I18n.translate('overview.widget.headerSection.refreshBtnTxt', 'IMT', 'Refresh'),
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

        if (Bpm.privileges.BpmManagement.canView()) {
            me.items[0].items.push(
                {
                    xtype: 'container',
                    layout: {
                        type: 'column'
                    },
                    items: [
                        {
                            xtype: 'work-list',
                            itemId: 'my-work-list',
                            router: me.router,
                            columnWidth: 0.33,
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
                                                label: Uni.I18n.translate('widget.myWorkList.property.processInstancesId', 'IMT', 'Process Id')
                                            },
                                            {
                                                name: 'processName',
                                                label: Uni.I18n.translate('widget.myWorkList.property.processName', 'IMT', 'Process name')
                                            }
                                        ]
                                    }
                                ]
                            }
                        }
                    ]
                }
            );
        }

        this.callParent(arguments);
    }
});