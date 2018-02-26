/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.insight.dataqualitykpi.view.AddManagement', {
    extend: 'Ext.form.Panel',
    requires: [
        'Cfg.insight.dataqualitykpi.view.fields.Purpose'
    ],
    alias: 'widget.ins-data-quality-kpi-add-mgm',
    defaults: {
        labelWidth: 250,
        width: 600
    },

    usagePointGroupsIsDefined: true,

    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'combobox',
                itemId: 'cmb-usage-point-group',
                name: 'usagePointGroup',
                required: true,
                fieldLabel: Uni.I18n.translate('general.uagePointGroup', 'CFG', 'Usage point group'),
                emptyText: Uni.I18n.translate('ins.dataqualitykpi.selectUsagePointGroup', 'CFG', 'Select a usage point group...'),
                store: 'Cfg.insight.dataqualitykpi.store.UsagePointGroups',
                queryMode: 'local',
                forceSelection: true,
                displayField: 'name',
                valueField: 'id'
            },
            {
                xtype: 'displayfield',
                itemId: 'no-usage-point-group-msg',
                required: true,
                fieldLabel: Uni.I18n.translate('general.uagePointGroup', 'CFG', 'Usage point group'),
                fieldStyle: 'color: #eb5642;',
                value: Uni.I18n.translate('ins.dataqualitykpi.noUsagePointGroup', 'CFG', 'No usage point group available.'),
                hidden: true
            },
            {
                xtype: 'purpose-field',
                itemId: 'fld-purposes',
                name: 'purposes',
                required: true,
                fieldLabel: Uni.I18n.translate('general.Purpose', 'CFG', 'Purpose'),
                hidden: true
            },
            {
                xtype: 'displayfield',
                itemId: 'view-purpose',
                required: true,
                fieldLabel: Uni.I18n.translate('general.Purpose', 'CFG', 'Purpose'),
                hidden: true
            },
            {
                xtype: 'combobox',
                itemId: 'cmb-frequency',
                name: 'frequency',
                required: true,
                fieldLabel: Uni.I18n.translate('datavalidationkpis.calculationFrequency', 'CFG', 'Calculation frequency'),
                emptyText: Uni.I18n.translate('datavalidationkpis.selectCalculationFrequency', 'CFG', 'Select a calculation frequency...'),
                store: 'Cfg.store.DataValidationKpiFrequency',
                queryMode: 'local',
                forceSelection: true,
                displayField: 'name',
                valueField: 'id'
            },
            {
                xtype: 'combobox',
                itemId: 'followedBy-combo',
                fieldLabel: Uni.I18n.translate('general.followedBy', 'CFG', 'Followed by'),
                name: 'nextRecurrentTasks',
                multiSelect: true,
                queryMode: 'local',
                store: 'Cfg.store.AllTasks',
                editable: false,
                emptyText: Uni.I18n.translate('addDataExportTask.taskSelectorPrompt', 'CFG', 'Select a task ...'),
                displayField: 'displayName',
                valueField: 'id'
            }
        ];
        me.callParent(arguments);
    },

    onAfterRender: function () {
        var me = this,
            usagePointGroupCombo = me.down('#cmb-usage-point-group');

        if (usagePointGroupCombo) {
            usagePointGroupCombo.focus();
        }
    },

    loadRecord: function (record) {
        var me = this,
            usagePointGroup = record.get('usagePointGroup'),
            frequency = record.get('frequency'),
            usagePointGroupCombo = me.down('[name=usagePointGroup]'),
            purpose = me.down('#view-purpose'),
            nextRecurrentTasks = record.get('nextRecurrentTasks');

        me.getForm().loadRecord(record);
        if (usagePointGroup) {
            usagePointGroupCombo.setRawValue(usagePointGroup.name);
            purpose.setVisible(true);
            purpose.setValue(record.get('metrologyPurpose').name);
        }

        if (usagePointGroupCombo.getStore().getCount() == 0) {
            usagePointGroupCombo.setVisible(false);
            me.down('#no-usage-point-group-msg').setVisible(true);
        }

        if (frequency) {
            me.down('[name=frequency]').setValue(frequency.every.count + frequency.every.timeUnit);
        }

        if (nextRecurrentTasks) {
            var selectedTasks = [];
            Ext.Array.each(nextRecurrentTasks, function (nextRecurrentTask) {
                selectedTasks.push(nextRecurrentTask.id);
            });
            me.down('[name=nextRecurrentTasks]').setValue(selectedTasks);
        }
    }

});