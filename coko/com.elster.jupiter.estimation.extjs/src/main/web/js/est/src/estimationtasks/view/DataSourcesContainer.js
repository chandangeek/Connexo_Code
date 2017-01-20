Ext.define('Est.estimationtasks.view.DataSourcesContainer', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.est-data-sources-container',
    required: true,
    layout: 'vbox',
    appName: null,
    edit: false,
    msgTarget: 'under',
    defaults: {
        labelWidth: 250
    },
    initComponent: function () {
        var me = this;
        switch (me.appName) {
            case 'MultiSense':
                me.items = [
                    {
                        xtype: 'fieldcontainer',
                        layout: {
                            type: 'vbox'
                        },
                        fieldLabel: Uni.I18n.translate('estimationtasks.addEstimationTask.deviceGroup', 'EST', 'Device group'),
                        required: true,
                        items: [
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
                        ]
                    }

                ];
                break;

            case 'MdmApp':
                me.items = [
                    {
                        xtype: 'fieldcontainer',
                        layout: {
                            type: 'vbox'
                        },
                        fieldLabel: Uni.I18n.translate('validationTasks.general.usagePointGroup', 'EST', 'Usage point group'),
                        required: true,
                        items: [
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
                        ]
                    },
                    {
                        xtype: 'fieldcontainer',
                        fieldLabel: Uni.I18n.translate('estimationtasks.general.purpose', 'EST', 'Purpose'),
                        itemId: 'purpose-group-container',
                        layout: {
                            type: 'hbox',
                        },
                        items: [
                            {
                                xtype: 'combobox',
                                width: 235,
                                itemId: 'cbo-estimation-task-purpose',
                                editable: false,
                                queryMode: 'local',
                                displayField: 'displayValue',
                                valueField: 'id',
                                name: 'metrologyPurpose',
                                store: 'Est.estimationtasks.store.MetrologyPurposes',
                                emptyText: Uni.I18n.translate('estimationtasks.addValidationTask.purposePrompt', 'EST', 'Select a purpose...'),
                                disabled: false,
                                listeners: {
                                    change: function () {
                                        me.down('#reset-purpose-btn').enable();
                                    }
                                }
                            },
                            {
                                xtype: 'uni-default-button',
                                itemId: 'reset-purpose-btn',
                                disabled: true,
                                tooltip: Uni.I18n.translate('estimationtasks.addValidationTask.reset', 'EST', 'Reset'),
                                hidden: false
                            }
                        ]
                    }
                ];
                break;
        }
        me.callParent(arguments);

    },
    loadGroupStore: function(callback){
        var me = this,
            onGroupsLoad = function (records) {
                if (records && !records.length && me.rendered) {
                    Ext.suspendLayouts();
                    me.showNoItemsField();
                    Ext.resumeLayouts(true);
                }
                callback && callback();
            };

        switch (me.appName) {
            case 'MultiSense':
                me.down('#device-group-combo').store.load(onGroupsLoad);
                break;
            case 'MdmApp':
                me.down('#cbo-estimation-task-purpose').store.load({
                    callback: function(){
                        me.down('#usagePoint-group-id').store.load(onGroupsLoad);
                    }
                });
                break;
        }

    },
    setComboValue: function (record) {
        var me = this;

        switch (me.appName) {
            case 'MultiSense':
                me.down('#device-group-combo').setValue(me.down('#device-group-combo').store.getById(record.get('deviceGroup').id));
                break;
            case 'MdmApp':
                me.down('#usagePoint-group-id').setValue(me.down('#usagePoint-group-id').store.getById(record.get('usagePointGroup').id));
                if(record.get('metrologyPurpose')){
                    me.down('#cbo-estimation-task-purpose').setValue(me.down('#cbo-estimation-task-purpose').store.getById(record.get('metrologyPurpose').id));
                }
                break;
        }
    },
    getValue: function(){

    },

    showNoItemsField: function () {
        var me = this;
        Ext.suspendLayouts();
        switch (me.appName) {
            case 'MultiSense':
                me.down('#device-group-combo').hide();
                me.down('#device-group-combo').allowBlank = true;
                me.down('#no-device').show();
                break;
            case 'MdmApp' :
                me.down('#usagePoint-group-id').hide();
                me.down('#usagePoint-group-id').allowBlank = true;
                me.down('#purpose-group-container').hide();
                me.down('#no-usagePoint').show();
                break;
        }
        Ext.resumeLayouts(true);
        // me.combineErrors = true;
    },

    getGroupValue: function(){

    }
});

