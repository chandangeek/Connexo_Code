Ext.define('Dsh.view.connectionsbulk.Step1', {
    extend: 'Ext.panel.Panel',
    requires: [
        'Uni.util.FormErrorMessage',
        'Uni.view.grid.BulkSelection'
    ],
    alias: 'widget.connections-bulk-step1',
    router: null,
    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'uni-form-error-message',
                itemId: 'step1-error-message',
                width: 400,
                hidden: true
            },
            {
                xtype: 'bulk-selection-grid',
                itemId: 'connections-bulk-selection-grid',
                store: 'Dsh.store.ConnectionTasksBuffered',

                counterTextFn: function (count) {
                    return Uni.I18n.translatePlural(
                        'connection.bulk.counterText',
                        count,
                        'DSH',
                        '{0} connections selected'
                    );
                },

                allLabel: Uni.I18n.translate('connection.bulk.allLabel', 'DSH', 'All connections'),
                allDescription: Uni.I18n.translate('connection.bulk.allDescription', 'DSH', 'Select all connections (related to filters and grouping on the connections screen)'),

                selectedLabel: Uni.I18n.translate('connection.bulk.selectedLabel', 'DSH', 'Selected connections'),
                selectedDescription: Uni.I18n.translate('connection.bulk.selectedDescription', 'DSH', 'Select connections in table'),

                bottomToolbarHidden: true,

                radioGroupName: 'selected-connections',

                columns: [
                    {
                        itemId: 'Device',
                        text: Uni.I18n.translate('connection.widget.details.device', 'DSH', 'Device'),
                        dataIndex: 'device',
                        flex: 1,
                        renderer: function (val) {
                            return Uni.Auth.hasAnyPrivilege(['privilege.view.device', 'privilege.administrate.deviceData'])
                                ? '<a href="' + me.router.getRoute('devices/device').buildUrl({mRID: val.id}) + '">' + val.name + '</a>' : val.name;
                        }
                    },
                    {
                        itemId: 'connectionMethod',
                        text: Uni.I18n.translate('connection.widget.details.connectionMethod', 'DSH', 'Connection method'),
                        dataIndex: 'connectionMethod',
                        flex: 1,
                        renderer: function (val) {
                            return val ? val.name : ''
                        }
                    },
                    {
                        itemId: 'currentState',
                        text: Uni.I18n.translate('connection.widget.details.currentState', 'DSH', 'Current state'),
                        dataIndex: 'currentState',
                        flex: 1,
                        renderer: function (val) {
                            return val ? val.displayValue : ''
                        }
                    },
                    {
                        itemId: 'latestStatus',
                        text: Uni.I18n.translate('connection.widget.details.latestStatus', 'DSH', 'Latest status'),
                        dataIndex: 'latestStatus',
                        flex: 1,
                        renderer: function (val) {
                            return val ? val.displayValue : ''
                        }
                    },
                    {
                        itemId: 'latestResult',
                        text: Uni.I18n.translate('connection.widget.details.latestResult', 'DSH', 'Latest result'),
                        dataIndex: 'latestResult',
                        name: 'latestResult',
                        flex: 1,
                        renderer: function (val) {
                            return val ? val.displayValue : ''
                        }
                    },
                    {
                        dataIndex: 'taskCount',
                        itemId: 'taskCount',
                        renderer: function (val, metaData) {
                            metaData.tdCls = 'communication-tasks-status';
                            var template = '';
                            if (val.numberOfSuccessfulTasks || val.numberOfFailedTasks || val.numberOfIncompleteTasks) {
                                template += '<span class="icon-checkmark"></span>' + (val.numberOfSuccessfulTasks ? val.numberOfSuccessfulTasks : '0');
                                template += '<span class="icon-close"></span>' + (val.numberOfFailedTasks ? val.numberOfFailedTasks : '0');
                                template += '<span class="icon-stop2"></span>' + (val.numberOfIncompleteTasks ? val.numberOfIncompleteTasks : '0');
                            }
                            return template;
                        },
                        header: Uni.I18n.translate('connection.widget.details.commTasks', 'DSH', 'Communication tasks'),
                        flex: 2
                    },
                    {
                        itemId: 'startDateTime',
                        text: Uni.I18n.translate('connection.widget.details.startedOn', 'DSH', 'Started on'),
                        dataIndex: 'startDateTime',
                        renderer: function (value) {
                            return value ? Uni.DateTime.formatDateTimeShort(value) : '';
                        },
                        flex: 1
                    }
                ]
            },
            {
                xtype: 'component',
                itemId: 'selection-grid-error',
                cls: 'x-form-invalid-under',
                margin: '-30 0 0 0',
                html: Uni.I18n.translate('connection.bulk.selectionGridError', 'DSH', 'Select at least one connection'),
                hidden: true
            }
        ];

        me.callParent(arguments);
    }
});