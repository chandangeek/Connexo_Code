/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointmanagement.view.changeusagepointlifecycle.Step1', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.change-usage-point-life-cycle-step1',
    router: null,
    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'uni-form-error-message',
                itemId: 'form-errors',
                hidden: true,
                width: 460
            },
            {
                xtype: 'combobox',
                fieldLabel: Uni.I18n.translate('usagepointchangelifecycleexecute.wizard.usagePointLifeCycle', 'IMT', 'Usage point life cycle'),
                itemId: 'change-usage-point-life-cycle-combo',
                name: 'usagePointLifeCycleId',
                width: 500,
                labelWidth: 150,
                store: 'Imt.usagepointmanagement.store.UsagePointLifeCycles',
                emptyText: Uni.I18n.translate('usagepointchangelifecycleexecute.wizard.selectUsagePointLifeCycle', 'IMT', 'Select usage point life cycle...'),
                required: true,
                //allowBlank: false,
                editable: false,
                queryMode: 'local',
                margin: '20 0 20 0',
                lastQuery: '',
                displayField: 'name',
                valueField: 'id'
            }
        ];

        me.callParent(arguments);
    }
});