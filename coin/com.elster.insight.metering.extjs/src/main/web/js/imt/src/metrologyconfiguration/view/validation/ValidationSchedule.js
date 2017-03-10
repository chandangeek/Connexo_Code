/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.metrologyconfiguration.view.validation.ValidationSchedule', {
    extend: 'Uni.view.container.PreviewContainer',
    alias: 'widget.mc-validation-schedule',
    requires: [
        'Imt.metrologyconfiguration.store.PurposesWithValidationTasks',
        'Imt.metrologyconfiguration.view.validation.PurposeWithTasksGrid',
        'Imt.metrologyconfiguration.model.ValidationTask'
    ],

    router: null,
    metrologyConfig: null,
    purposes: null,
    selectByDefault: false,

    initComponent: function () {
        var me = this,
            data = [],
            store,
            tasksCount = 0;

        if (me.purposes && me.purposes.length) {
            me.emptyComponent = {
                xtype: 'no-items-found-panel',
                itemId: 'no-validation-tasks-found-panel',
                title: Uni.I18n.translate('usagepoint.dataValidation.schedule.emptyCmp.title', 'IMT', 'No validation tasks found'),
                reasons: [
                    Uni.I18n.translate('usagepoint.dataValidation.schedule.emptyCmp.item1', 'IMT', 'No validation tasks have been defined yet.'),
                    Uni.I18n.translate('usagepoint.dataValidation.schedule.emptyCmp.item2', 'IMT', 'Validation tasks exist, but you do not have permission to view them.')
                ],
                stepItems: [
                    {
                        itemId: 'metrology-config-manage-validation-schedule-btn',
                        text: Uni.I18n.translate('validation.tasks.manage', 'IMT', 'Manage validation tasks'),
                        privileges: Imt.privileges.MetrologyConfig.adminValidation,
                        disabled: me.metrologyConfig.get('status').id == 'deprecated',
                        href: me.router.getRoute('administration/validationtasks').buildUrl()
                    }
                ]
            };

            me.purposes.forEach(function (purpose) {
                if (purpose.validationTasks().getCount() == 0) {
                    data.push({
                        noTasks: true,
                        metrologyContract: purpose.get('name'),
                        metrologyContractIsMandatory: purpose.get('mandatory'),
                        metrologyContractId: purpose.getId()
                    });
                } else {
                    purpose.validationTasks().each(function (validationTask) {
                        data.push(Ext.merge(validationTask.getData(), {
                            metrologyContract: purpose.get('name'),
                            metrologyContractIsMandatory: purpose.get('mandatory'),
                            metrologyContractId: purpose.getId(),
                            uniqueId: validationTask.get('id') + ' ' + purpose.getId()
                        }));                        
                        tasksCount++;
                    });
                }
            });
        } else {
            me.emptyComponent = {
                xtype: 'no-items-found-panel',
                itemId: 'no-purposes-found-panel',
                title: Uni.I18n.translate('metrologyConfigPurposes.empty.title', 'IMT', 'No purposes found'),
                reasons: [
                    Uni.I18n.translate('purposes.empty.list.item', 'IMT', 'No purposes have been added yet.')
                ]
            };
        }
        store = Ext.create('Ext.data.Store', {
            model: 'Imt.metrologyconfiguration.model.ValidationTask',
            groupField: 'metrologyContract'
        });
        if (tasksCount !== 0) {
            store.loadRawData(data);
        }                
        store.totalCount = tasksCount;

        me.grid = {
            xtype: 'purpose-with-tasks-grid',
            itemId: 'purpose-with-tasks-grid',
            router: me.router,
            store: store,
            purposes: me.purposes,
            metrologyConfig: me.metrologyConfig
        };
        
        me.on('afterrender', function () {
            store.fireEvent('load');
            var index = store.findBy(function (record) {                
                return record.get('id');
            });
            me.down('#purpose-with-tasks-grid').getSelectionModel().select(index);
        }, me, {single: true});

        me.callParent(arguments);
    }
});

