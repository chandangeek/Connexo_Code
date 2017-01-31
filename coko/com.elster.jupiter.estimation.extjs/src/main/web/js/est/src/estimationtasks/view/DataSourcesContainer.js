/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Est.estimationtasks.view.DataSourcesContainer', {
    extend: 'Ext.form.FieldContainer',
    alias: 'widget.est-data-sources-container',
    required: true,
    layout: 'vbox',
    appName: null,
    edit: false,
    msgTarget: 'under',
    defaults: {
        width: 300
    },
    initComponent: function () {
        var me = this;

        switch (me.appName) {
            case 'MultiSense':
                me.fieldLabel = Uni.I18n.translate('estimationtasks.addEstimationTask.deviceGroup', 'EST', 'Device group');
                me.items = [
                    {
                        xtype: 'combobox',
                        itemId: 'device-group-combo',
                        name: 'deviceGroupId',
                        width: 235,
                        store: 'Est.estimationtasks.store.DeviceGroups',
                        editable: false,
                        disabled: false,
                        emptyText: Uni.I18n.translate('estimationtasks.addEstimationTask.deviceGroupPrompt', 'EST', 'Select a device group...'),
                        allowBlank: false,
                        queryMode: 'local',
                        displayField: 'name',
                        valueField: 'id'
                    },
                    {
                        xtype: 'displayfield',
                        itemId: 'no-device',
                        hidden: true,
                        value: '<div style="color: #eb5642">' + Uni.I18n.translate('estimationtasks.general.noDeviceGroup', 'EST', 'No device group defined yet.') + '</div>',
                        htmlEncode: false,
                        width: 235
                    }
                ];
                break;

            case 'MdmApp':
                me.fieldLabel = Uni.I18n.translate('estimationtasks.addEstimationTask.usagePointGroup', 'EST', 'Usage point group');
                me.items = [
                    {
                        xtype: 'combobox',
                        itemId: 'usagePoint-group-id',
                        name: 'usagePointGroup',
                        width: 235,
                        store: 'Est.estimationtasks.store.UsagePointGroups',
                        editable: false,
                        disabled: false,
                        emptyText: Uni.I18n.translate('estimationtasks.addEstimationTask.usagePointGroupPrompt', 'EST', 'Select a usage point group...'),
                        allowBlank: false,
                        queryMode: 'local',
                        displayField: 'displayValue',
                        valueField: 'id'
                    },
                    {
                        xtype: 'displayfield',
                        itemId: 'no-usagePoint',
                        hidden: true,
                        value: '<div style="color: #FF0000">' + Uni.I18n.translate('estimationtasks.general.noUsagePointGroup', 'EST', 'No usage point group defined yet.') + '</div>',
                        htmlEncode: false,
                        width: 235
                    }
                ];
                break;


        }
        me.callParent(arguments);
    },
    loadGroupStore: function(callback){
        var me =this,
            groupCombo = me.down('combobox'),
            noGroups = me.down('displayfield');
        groupCombo.store.load(function () {
            if (this.getCount() === 0) {
                groupCombo.allowBlank = true;
                groupCombo.hide();
                noGroups.show();
            }
            callback && callback();
        });
    },
    setComboValue: function(value){
        var me = this;
        me.down('combobox').setValue(me.down('combobox').store.getById(value));
    },
    getValue: function(){

    },

    getGroupValue: function(){

    }
});

