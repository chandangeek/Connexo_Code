Ext.define('Imt.metrologyconfiguration.view.validation.PurposeWithTasksGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.purpose-with-tasks-grid',
    purposes: null,
    router: null,
    metrologyConfig: null,
    requires: [
        'Cfg.store.DaysWeeksMonths'
    ],

    initComponent: function () {
        var me = this,
            periodsStore = Ext.getStore('Cfg.store.DaysWeeksMonths');

        me.features = [{
            ftype: 'grouping',
            groupHeaderTpl: [
                '{name} {name:this.createHeader}',
                {
                    createHeader: function (name) {
                        var store = me.getStore(),
                            record = store.getAt(store.find('metrologyContract', name)),
                            count = 0,
                            tasksCount;

                        if (!record.get('noTasks')) {
                            store.each(function (record) {
                                if (record.get('metrologyContract') == name) {
                                    count++;
                                }
                            });
                        } else {

                        }

                        tasksCount = Uni.I18n.translatePlural('validationTasks.count', count, 'IMT',
                            'No validation tasks',
                            '{0} validation task',
                            '{0} validation tasks');

                        return record.get('metrologyContractIsMandatory') ? '<span class="uni-form-item-label-required" style="display: inline-block; width: 16px; height: 16px;"></span> (' + tasksCount + ')' : '(' + tasksCount + ')';
                    }
                }
            ]
        }];

        me.columns = [
            {
                header: Uni.I18n.translate('general.label.name', 'IMT', 'Name'),
                dataIndex: 'name',
                flex: 1,
                renderer: function (value, metaData, record) {
                    return '<a href="' + me.router.getRoute('administration/validationtasks/validationtask').buildUrl({taskId: record.get('id')}) + '">' + Ext.String.htmlEncode(value) + '</a>';
                }
            },
            {
                header: Uni.I18n.translate('validation.schedule', 'IMT', 'Schedule'),
                dataIndex: 'schedule',
                renderer: function (value, metaData, record) {
                    //todo: replace started on?
                    return Ext.isEmpty(value)
                        ? Uni.I18n.translate('metrologyConfiguration.validation.schedule.manual', 'IMT', 'On request')
                        : Uni.I18n.translate('metrologyConfiguration.validation.schedule.manual', 'IMT', 'Every {0} {1} started on {2}', [
                            value.count,
                            periodsStore.findRecord('name', value.timeUnit).get('displayValue'),
                            record.get('nextRun') ? Uni.DateTime.formatDateTimeLong(record.get('nextRun')) : '-'
                    ]);
                },
                flex: 1
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                itemId: 'purpose-rule-sets-grid-pagingtoolbartop',
                dock: 'top',
                store: me.store,
                displayMsg: Uni.I18n.translate('metrologyConfiguration.validation.tasks.count', 'IMT', '{0} validation task(s)', me.getStore().totalCount),
                noBottomPaging: true,
                usesExactCount: true
            }
        ];

        me.callParent(arguments);
    }
});
