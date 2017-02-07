Ext.define('Dal.view.bulk.AlarmsSelectionGrid', {
    extend: 'Uni.view.grid.BulkSelection',
    xtype: 'alarms-selection-grid',

    counterTextFn: function (count) {
        return Uni.I18n.translatePlural('general.nrOfAlarmss.selected', count, 'DAL',
            'No alarms selected', '{0} alarm selected', '{0} alarms selected'
        );
    },

    allLabel: Uni.I18n.translate('workspace.alarms.bulk.AlarmsSelectionGrid.allLabel', 'DAL', 'All alarms'),
    allDescription: Uni.I18n.translate('workspace.alarms.bulk.AlarmsSelectionGrid.allDescription', 'DAL', 'Select all alarms (related to filters and grouping on the alarms screen)'),

    selectedLabel: Uni.I18n.translate('workspace.alarms.bulk.AlarmsSelectionGrid.selectedLabel', 'DAL', 'Selected alarms'),
    selectedDescription: Uni.I18n.translate('workspace.alarms.bulk.AlarmsSelectionGrid.selectedDescription', 'DAL', 'Select alarms in table'),

    cancelHref: '#/search',

    columns: {
        items: [
            {
                itemId: 'alarms-grid-id',
                header: Uni.I18n.translate('general.title.alarmId', 'DAL', 'ID'),
                dataIndex: 'alarmId'
            },
            {
                itemId: 'alarms-grid-title',
                header: Uni.I18n.translate('general.title.alarm', 'DAL', 'Alarm'),
                dataIndex: 'title',
                flex: 2
            },
            {
                itemId: 'alarms-grid-due-date',
                header: Uni.I18n.translate('general.title.dueDate', 'DAL', 'Due date'),
                dataIndex: 'dueDate',
                renderer: function (value) {
                    return value ? Uni.DateTime.formatDateShort(value) : '';
                },
                width: 140
            },
            {
                itemId: 'alarms-grid-status',
                header: Uni.I18n.translate('general.status', 'DAL', 'Status'),
                dataIndex: 'status_name',
                width: 100
            },
            {
                itemId: 'alarms-grid-workgroup-assignee',
                header: Uni.I18n.translate('general.workgroup', 'DAL', 'Workgroup'),
                dataIndex: 'workGroupAssignee',
                flex: 1,
                renderer: function (value, metaData, record, rowIndex, colIndex) {
                    var result;

                    if (!Ext.isEmpty(value) && value.hasOwnProperty('id')) {
                        result = '';

                        result += '<span class="isu-icon-GROUP isu-assignee-type-icon" data-qtip="';
                        result += Uni.I18n.translate('assignee.tooltip.workgroup', 'DAL', 'Workgroup');
                        result += '"></span>';

                        if (value.name) {
                            result += Ext.String.htmlEncode(value.name);
                        }
                    } else {
                        result = '-'
                    }
                    return result || this.columns[colIndex].emptyText;
                }
            },
            {
                itemId: 'alarms-grid-user-assignee',
                header: Uni.I18n.translate('general.user', 'DAL', 'User'),
                dataIndex: 'userAssignee',
                flex: 1,
                renderer: function (value, metaData, record, rowIndex, colIndex) {
                    var result

                    if (value && value.hasOwnProperty('id')) {
                        var result = '';

                        result += '<span class="isu-icon-USER isu-assignee-type-icon" data-qtip="';
                        result += Uni.I18n.translate('assignee.tooltip.USER', 'DAL', 'User');
                        result += '"></span>';

                        if (value.name) {
                            result += Ext.String.htmlEncode(value.name);
                        }
                    } else {
                        result = '-';
                    }

                    return result || this.columns[colIndex].emptyText;
                }
            }
        ]
    },

    initComponent: function () {
        this.callParent(arguments);
        this.getBottomToolbar().setVisible(false);
    }
});