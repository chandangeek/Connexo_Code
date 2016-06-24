Ext.define('Bpm.view.task.bulk.TaskGroupsGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.bpm-tasks-occurences-grid',
    store: 'Bpm.store.task.TaskGroups',
    requires: [
        'Uni.grid.column.Duration',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom'

    ],

    initComponent: function () {
        var me = this;
        me.columns = [
            {
                header: Uni.I18n.translate('general.status', 'BPM', 'Status'),
                dataIndex: 'hasMandatory',
                renderer: function (value) {
                        if (value) {
                            var mandatoryFields = Uni.I18n.translate('bpm.task.bulk.mandatoryFields', 'BPM', 'Mandatory fields require attention');
                            return '<div title="' + mandatoryFields + '" class="icon-cancel-circle2" style="margin-left: 10px; color: #EB5642; font-size:15px"></div>';
                            }
                        else {
                            return '<div class="' + Uni.About.baseCssPrefix + 'default-column-icon' + ' default" style="margin-left: 10px">&nbsp;</div>';
                        }

                    }

            },
            {
                header: Uni.I18n.translate('bpm.task.name', 'BPM', 'Task'),
                dataIndex: 'name',
                flex: 1,
                renderer: function (value) {
                    return Ext.isEmpty(value) ? '-' : value;
                }
            },
            {
                header: Uni.I18n.translate('bpm.task.process', 'BPM', 'Process'),
                dataIndex: 'processName',
                flex: 1
            },
            {
                header: Uni.I18n.translate('bpm.task.version', 'BPM', 'Version'),
                dataIndex: 'version',
                flex: 1
            },

            {
                header: Uni.I18n.translate('bpm.task.occurences', 'BPM', 'Occurrences'),
                dataIndex: 'count',
                flex: 1
            },
            {
                xtype: 'actioncolumn',
                align: 'right',
                renderer: function () {
                    if(me.getStore('Bpm.store.task.TaskGroups').data.items.length <= 1)
                    {
                        this.items[0].iconCls = 'uni-icon-deleted';
                        this.items[0].disabled = true;
                    }
                    else
                    {
                        this.items[0].iconCls = 'uni-icon-delete';
                        this.items[0].disabled = false;
                    }
                },
                items: [
                    {
                        tooltip: Uni.I18n.translate('bpm.task.bulk.remove', 'BPM', 'Remove'),
                        handler: function (grid, rowIndex, colIndex, column, event, record) {
                            grid.getStore().removeAt(rowIndex);
                            grid.refresh();
                            if(grid.getStore().getCount() <= 1) {
                                this.items[0].disabled = true;
                                this.items[0].iconCls = 'uni-icon-deleted';
                                grid.refresh();
                            }
                            else
                            {
                                this.items[0].disabled = false;
                                this.items[0].iconCls = 'uni-icon-delete';
                            }
                        }
                    }
                ],
            }

        ],
        me.callParent(arguments);
    },
});
