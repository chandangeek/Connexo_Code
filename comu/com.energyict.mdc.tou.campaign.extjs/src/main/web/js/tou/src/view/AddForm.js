/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Tou.view.AddForm', {
    extend: 'Ext.form.Panel',
    requires: [
        'Uni.util.FormErrorMessage',
        'Uni.property.form.Property',
        'Uni.form.field.TimeInHoursAndMinutes',
        'Ext.form.RadioGroup',
        'Tou.view.ActivateCalendar',
        'Tou.store.DaysWeeksMonths',
        'Tou.store.ComTasks',
        'Uni.property.view.DefaultButton'
    ],
    alias: 'widget.tou-campaigns-add-form',
    returnLink: null,
    action: null,
    skipLoadingIndication: false,
    campaignRecordBeingEdited: null,

    defaults: {
        labelWidth: 260,
        width: 600,
        msgTarget: 'under'
    },

    loadRecord: function(record) {
        var me = this;
        var sendCalendarComTask = record.get('sendCalendarComTask');
        var validationComTask = record.get('validationComTask');
        var sendCalendarConnectionStrategy = record.get('sendCalendarConnectionStrategy');
        var validationConnectionStrategy = record.get('validationConnectionStrategy');

        me.callParent(arguments);

        me.getForm().setValues({
            sendCalendarComTask: sendCalendarComTask && sendCalendarComTask.id,
            validationComTask: validationComTask && validationComTask.id,
            sendCalendarConnectionStrategy: sendCalendarConnectionStrategy
                ? sendCalendarConnectionStrategy.id
                : me.defaultConnectionStrategy,
            validationConnectionStrategy: validationConnectionStrategy
                ? validationConnectionStrategy.id
                : me.defaultConnectionStrategy
        })
    },
    initComponent: function () {
        var me = this;

        me.items = [{
                xtype: 'uni-form-error-message',
                itemId: 'form-errors',
                name: 'form-errors',
                margin: '0 0 10 0',
                hidden: true
            }, {
                xtype: 'textfield',
                itemId: 'tou-campaign-name',
                name: 'name',
                fieldLabel: Uni.I18n.translate('general.name', 'TOU', 'Name'),
                required: true,
                allowBlank: false
            }, {
                xtype: 'combobox',
                itemId: 'tou-campaign-device-type',
                name: 'deviceType',
                fieldLabel: Uni.I18n.translate('general.deviceType', 'TOU', 'Device type'),
                required: true,
                allowBlank: false,
                store: 'Tou.store.DeviceTypes',
                forceSelection: true,
                queryMode: 'local',
                displayField: 'name',
                valueField: 'id',
                listeners: {
                    change: {
                        fn: Ext.bind(me.onDeviceTypeChange, me)
                    }
                }
            }, {
                xtype: 'fieldcontainer',
                fieldLabel: Uni.I18n.translate('general.deviceGroup', 'TOU', 'Device group'),
                itemId: 'tou-campaign-device-group-field-container',
                required: true,
                allowBlank: false,
                layout: 'hbox',
                width: 650,
                items: [{
                        xtype: 'combobox',
                        itemId: 'tou-campaign-device-group',
                        name: 'deviceGroup',
                        store: 'Tou.store.DeviceGroups',
                        forceSelection: true,
                        allowBlank: false,
                        queryMode: 'local',
                        displayField: 'name',
                        valueField: 'name',
                        required: true,
                        width: 325
                    }
                ]
            }, {
                xtype: 'timeInHoursAndMinutes',
                fieldLabel: Uni.I18n.translate('general.timeBoundaryStart', 'TOU', 'Time boundary start'),
                name: 'activationStart',
                itemId: 'activationStart',
                required: true,
                allowBlank: false
            }, {
                xtype: 'timeInHoursAndMinutes',
                fieldLabel: Uni.I18n.translate('general.timeBoundaryEnd', 'TOU', 'Time boundary end'),
                name: 'activationEnd',
                itemId: 'activationEnd',
                required: true,
                allowBlank: false
            }, {
                xtype: 'activate-calendar-field',
                itemId: 'activate-calendar',
                id: 'tou-activate-calendar',
                name: 'activation',
                fieldLabel: Uni.I18n.translate('general.activateCalendar', 'TOU', 'Activate calendar'),
                layout: 'hbox',
                width: 800,
                required: true,
                allowBlank: false,
                hidden: true,
                listeners: {
                    change: me.onActiveCalendarChange,
                    scope: me
                }
            }, {
                xtype: 'fieldcontainer',
                name:'assign',
                hidden: true,
                itemId: 'activateCalendarErrorMain',
                fieldLabel: ' ',
                layout: 'vbox',
                width: 600,
                items:
                    [{
                        xtype: 'component',
                        itemId: 'activateCalendarError',
                        cls: 'x-form-invalid-under',
                        margin: '-12 0 0 0',
                        html: Uni.I18n.translate('general.required.field', 'TOU', 'This field is required')
                    }]
            }, {
                xtype: 'displayfield',
                itemId: 'tou-update-type-disp',
                fieldLabel: Uni.I18n.translate('general.updateOption', 'TOU', 'Update'),
                hidden: true
            }, {
                xtype: 'radiogroup',
                itemId: 'tou-update-type',
                name: 'updateOption',
                fieldLabel: Uni.I18n.translate('general.updateOption', 'TOU', 'Update'),
                required: true,
                hidden: true,
                vertical: true,
                columns: 1,
                renderTo: Ext.getBody(),
                items: [{
                        id: 'fullCalendar',
                        boxLabel: Uni.I18n.translate('general.fullCalendar', 'TOU', 'Full calendar'),
                        inputValue: 'fullCalendar',
                        name: 'updateType',
                        margin: '5 0 5 0'
                    }, {
                        id: 'specialDays',
                        boxLabel: Uni.I18n.translate('general.specialDays', 'TOU', 'Only special days'),
                        inputValue: 'specialDays',
                        name: 'updateType',
                        margin: '5 0 5 0'
                    }
                ],
                width: 1000
            }, {
                itemId: 'tou-period-values',
                id: 'tou-period-values',
                xtype: 'fieldcontainer',
                name: 'validationTimeout',
                fieldLabel: Uni.I18n.translate('general.touTimeout', 'TOU', 'Timeout before validation'),
                margin: '10 0 10 0',
                required: true,
                layout: 'hbox',
                hidden: true,
                items: [{
                        itemId: 'period-number',
                        xtype: 'numberfield',
                        name: 'recurrenceNumber',
                        allowDecimals: false,
                        valueField: 'id',
                        minValue: 1,
                        value: 1,
                        width: 65,
                        margin: '0 10 0 0'
                    }, {
                        itemId: 'period-combo',
                        xtype: 'combobox',
                        store: Ext.create('Tou.store.DaysWeeksMonths'),
                        displayField: 'displayValue',
                        valueField: 'id',
                        queryMode: 'local',
                        width: 100,
                        listeners: {
                            afterrender: function () {
                                var defaultVal = this.store.getAt('1');
                                this.setValue(defaultVal);
                            }
                        }
                    }
                ]
            }, {
                xtype: 'combobox',
                itemId: 'tou-campaign-allowed-calendar',
                name: 'Calendar',
                fieldLabel: Uni.I18n.translate('general.touCalendar', 'TOU', 'Time of use calendar'),
                required: true,
                allowBlank: false,
                forceSelection: true,
                queryMode: 'local',
                displayField: 'name',
                valueField: 'id',
                margin: '10 0 10 0',
                hidden: true
            }, {
                itemId: 'unique-calendar-name-field',
                xtype: 'checkbox',
                fieldLabel: Uni.I18n.translate(
                    'general.uniqueCalendarName',
                    'TOU',
                    'Upload with unique calendar name'
                ),
                name: 'withUniqueCalendarName',
                margin: '-5 0 0 0'
            }, {
                xtype: 'combobox',
                itemId: 'tou-campaign-allowed-comtask',
                name: 'sendCalendarComTask',
                store: 'Tou.store.ComTasks',
                fieldLabel: Uni.I18n.translate(
                    'general.sendCalendarComTask',
                    'TOU',
                    'Calendar upload communication task'
                ),
                required: true,
                allowBlank: false,
                forceSelection: true,
                emptyText: Uni.I18n.translate(
                    'general.sendCalendarComTask.empty',
                    'TOU',
                    'Select communication task ...'
                ),
                queryMode: 'local',
                displayField: 'name',
                valueField: 'id',
                margin: '10 0 10 0',
                hidden: true,
            },
            {
                xtype: 'fieldcontainer',
                layout: 'hbox',
                itemId: 'tou-campaign-send-connection-strategy-container',
                hidden: true,
                fieldLabel: Uni.I18n.translate(
                    'general.connectionMethodStrategy',
                    'TOU',
                    'Connection method strategy'
                ),
                items: [
                    {
                        xtype: 'combobox',
                        itemId: 'tou-campaign-send-connection-strategy',
                        name: 'sendCalendarConnectionStrategy',
                        store: 'Tou.store.ConnectionStrategy',
                        queryMode: 'local',
                        displayField: 'name',
                        margin: '0 10 0 0',
                        valueField: 'id',
                        listeners: {
                            change: function(field, val) {
                                me.down('#tou-campaign-send-connection-strategy-reset')
                                .setDisabled(!val);
                            },
                            scope: me,
                        }
                    },
                    {
                        xtype: 'uni-default-button',
                        itemId: 'tou-campaign-send-connection-strategy-reset',
                        handler: function() {
                            this.down('[name=sendCalendarConnectionStrategy]').reset();
                        },
                        scope: me,
                        margin: '0 0 0 10',
                        hidden: false,
                        disabled: true
                    }
                ]
            },
            {
                xtype: 'combobox',
                itemId: 'tou-campaign-validation-comtask',
                name: 'validationComTask',
                store: 'Tou.store.ComTasks',
                fieldLabel: Uni.I18n.translate(
                    'general.validationComTask',
                    'TOU',
                    'Validation communication task'
                ),
                disabled: true,
                required: true,
                allowBlank: false,
                forceSelection: true,
                emptyText: Uni.I18n.translate(
                    'general.validationComTask.empty',
                    'TOU',
                    'Select communication task ...'
                ),
                queryMode: 'local',
                displayField: 'name',
                valueField: 'id',
                hidden: true
            },
            {
                xtype: 'fieldcontainer',
                layout: 'hbox',
                itemId: 'tou-campaign-validation-strategy-container',
                hidden: true,
                disabled: true,
                fieldLabel: Uni.I18n.translate(
                    'general.connectionMethodStrategy',
                    'TOU',
                    'Connection method strategy'
                ),
                items: [
                    {
                        xtype: 'combobox',
                        itemId: 'tou-campaign-validation-connection-strategy',
                        name: 'validationConnectionStrategy',
                        store: 'Tou.store.ConnectionStrategy',
                        queryMode: 'local',
                        displayField: 'name',
                        margin: '0 10 0 0',
                        valueField: 'id',
                        listeners: {
                            change: function(field, val) {
                                me.down('#tou-campaign-validation-connection-strategy-reset')
                                .setDisabled(!val);
                            },
                            scope: me,
                        }
                    },
                    {
                        xtype: 'uni-default-button',
                        itemId: 'tou-campaign-validation-connection-strategy-reset',
                        handler: function() {
                            this.down('[name=validationConnectionStrategy]').reset();
                        },
                        scope: me,
                        margin: '0 0 0 10',
                        hidden: false,
                        disabled: true
                    }
                ]
            },
            {
                xtype: 'fieldcontainer',
                itemId: 'form-buttons',
                fieldLabel: '&nbsp;',
                layout: 'hbox',
                margin: '20 0 0 0',
                items: [{
                        xtype: 'button',
                        itemId: 'btn-add-tou-campaign',
                        text: Uni.I18n.translate('general.add', 'TOU', 'Add'),
                        ui: 'action',
                        action: me.action
                    }, {
                        xtype: 'button',
                        itemId: 'btn-cancel-add-tou-campaign',
                        text: Uni.I18n.translate('general.cancel', 'TOU', 'Cancel'),
                        ui: 'link',
                        action: 'cancel',
                        href: me.returnLink
                    }
                ]
            }
        ];

        me.callParent(arguments);

        Ext.Array.each(Ext.ComponentQuery.query('uni-default-button'), function(item){
           item.setTooltip('Restore to default empty value');
        })
    },

    onActiveCalendarChange: function (field, newValue) {
        var me = this;
        var value = newValue.activateCal;

        Ext.suspendLayouts();
        if (value && (value === 'immediately' || value === 'onDate')) {
            me.down('[name=validationComTask]').show();

            me.down('#tou-campaign-validation-strategy-container').show();
            if (!me.campaignRecordBeingEdited) {
                me.down('[name=validationComTask]').setDisabled(false);
                me.down('#tou-campaign-validation-strategy-container').setDisabled(false);
            }
        } else {
            me.down('[name=validationComTask]').hide();
            me.down('[name=validationComTask]').setDisabled(true);
            me.down('#tou-campaign-validation-strategy-container').hide();
            me.down('#tou-campaign-validation-strategy-container').setDisabled(true);
        }
        Ext.resumeLayouts(true);
    },

    setUpdateTypeLabel: function (option){
         switch (option){
             case 'fullCalendar':
                return Uni.I18n.translate('general.fullCalendar', 'TOU', 'Full calendar');
             case 'specialDays':
                return Uni.I18n.translate('general.specialDays', 'TOU', 'Only special days');
             default:
                return null;
         }
    },
    enableUpdateOptions: function (allEnabledOptionsArr){
	    var allOptions = ['fullCalendar', 'specialDays'],
	        me = this,
	        radiogroup = me.down('#tou-update-type'),
	        displayField = me.down('#tou-update-type-disp');

	    if (!allEnabledOptionsArr || !allEnabledOptionsArr.length){
	        radiogroup.hide();
	        return;
	    }
	    if (allEnabledOptionsArr.length == 1){
            radiogroup.hide();
            displayField.show();
            displayField.setValue(me.setUpdateTypeLabel(allEnabledOptionsArr[0]));
            var value = {};
            value['updateType'] = allEnabledOptionsArr[0];
            radiogroup.setValue(value);
	    }else{
	        displayField.hide();
	        radiogroup.show();
	        radiogroup.setValue({});
            for ( var optCnt = 0; optCnt < allOptions.length; optCnt++ ){
                var option = allOptions[optCnt];
                var cmp = Ext.getCmp(option);
                if (cmp){
                   (Ext.Array.indexOf(allEnabledOptionsArr, option) !== -1) ? cmp.show() : cmp.hide()
                }
            }

            if (allEnabledOptionsArr.indexOf('fullCalendar') !== -1){
                var value = {};
                value['updateType'] = allEnabledOptionsArr[0];
                radiogroup.setValue(value);
            }
        }
    },
    onDeviceTypeChange: function (radiogroup, newValue) {
        var me = this;
        var record = me.getRecord();
        if (!radiogroup.findRecordByValue(newValue)) return;

        var activateCalendarItem = me.down('#activate-calendar');
        var sendComtaskField = me.down("[name=sendCalendarComTask]");
        activateCalendarItem.show();
        me.down('#tou-update-type').show();
        me.down('#tou-campaign-send-connection-strategy-container').show();
        sendComtaskField.show();

        var cbxCal = me.down('#tou-campaign-allowed-calendar');
        cbxCal.show();

        var calStore = Ext.create('Tou.store.AllowedDeviceTypeOptions');
        calStore.getProxy().setUrl(newValue);
        sendComtaskField.getStore().getProxy().setUrl(newValue);
        sendComtaskField.getStore().load(function(){
             sendComtaskField.setValue(record.get('sendCalendarComTask') && record.get('sendCalendarComTask').id);
        });

        calStore.load(function () {
            var calParams = calStore.getAt(0);
            if (!calParams)
                return;
            cbxCal.bindStore(calParams.calendars());

            var allEnabledActivateCalendarOptionsArr = [],
                allEnabledUpdateTypeOptionsArr = [];

            function setAllowedDeviceTypeOptions(record){
                 var enabledActivateCalendarOptions = [],
                     enabledUpdateTypeOptions = [],
                     deviceTypeOptions = ['fullCalendar', 'withActivationDate', 'specialDays'];

                 Ext.Array.forEach(deviceTypeOptions, function(paramName){
                     switch(paramName){
                         case 'fullCalendar':
                            enabledActivateCalendarOptions = ['withoutActivation'];
                            enabledUpdateTypeOptions = ['fullCalendar'];
                            break;

                         case 'withActivationDate':
                            enabledActivateCalendarOptions = ['immediately', 'onDate'];
                            enabledUpdateTypeOptions = ['fullCalendar'];
                            break;

                         case 'specialDays':
                            enabledActivateCalendarOptions = ['withoutActivation'];
                            enabledUpdateTypeOptions = ['specialDays'];
                            break;

                         default:
                            break;
                     }

                     if (record.get(paramName)){
                         allEnabledActivateCalendarOptionsArr = Ext.Array.merge(allEnabledActivateCalendarOptionsArr, enabledActivateCalendarOptions);
                         allEnabledUpdateTypeOptionsArr = Ext.Array.merge(allEnabledUpdateTypeOptionsArr, enabledUpdateTypeOptions);
                     }
                 });

            }

            setAllowedDeviceTypeOptions(calParams);

            activateCalendarItem.setOptions(allEnabledActivateCalendarOptionsArr);
            me.enableUpdateOptions(allEnabledUpdateTypeOptionsArr);

            me.fireEvent('tou-deviceTypeChanged');
        });
    },
    loadRecordForEdit: function (campaignRecord) {
        var me = this,
        deviceTypeCombo = me.down('#tou-campaign-device-type'),
        deviceGroupComboContainer = me.down('#tou-campaign-device-group-field-container'),
        deviceGroupCombo = me.down('#tou-campaign-device-group'),
        periodCombo = me.down('#period-combo'),
        periodNumber = me.down('#period-number'),
        periodValues = me.down('#tou-period-values'),
        deviceTypeId = campaignRecord.get('deviceType').id,
        DeviceGroupComboAndSetDeviceType = function () {
            deviceGroupCombo.setDisabled(true);
            deviceGroupCombo.setRawValue(campaignRecord.get('deviceGroup'));
            deviceTypeCombo.setDisabled(true);
            deviceTypeCombo.setValue(deviceTypeId);
        },
        setTimeoutFld = function (validationTimeout) {
            if (validationTimeout) {
                var timeUnit = 'minutes';
                validationTimeout = validationTimeout / 60;
                if (validationTimeout % 60) {
                    validationTimeout = validationTimeout / 60;
                    timeUnit = 'hours';
                }
                if (validationTimeout % 24) {
                    validationTimeout = validationTimeout / 24;
                    timeUnit = 'days';
                }
                if (validationTimeout % 7) {
                    validationTimeout = validationTimeout / 7;
                    timeUnit = 'weeks';
                }
                periodNumber.setValue(validationTimeout);
                periodCombo.setRawValue(timeUnit);
            } else {
                periodNumber.setValue(0);
                periodCombo.setRawValue('minutes');
            }
        },
        setOptions = function () {
            periodValues.setDisabled(true);

            setTimeoutFld(campaignRecord.get('validationTimeout'));
            var timeStartInSec = new Date(campaignRecord.get('activationStart') / 1000);
            var timeEndInSec = new Date(campaignRecord.get('activationEnd') / 1000);
            me.down("#activationStart").setValue(timeStartInSec.getHours() * 3600 + timeStartInSec.getMinutes() * 60);
            me.down("#activationEnd").setValue(timeEndInSec.getHours() * 3600 + timeEndInSec.getMinutes() * 60);
            me.down("#activate-calendar").setDisabled(true);
            me.down('#uploadRadioGroup').setValue({ activateCal: campaignRecord.get('activationOption') })
            me.down("#tou-campaign-allowed-calendar").setValue(campaignRecord.get('calendar').id);
            me.down("#tou-campaign-allowed-calendar").setDisabled(true);
            me.down("#tou-update-type").setValue(campaignRecord.get('updateType'));
            me.down("#tou-update-type").setDisabled(true);
            me.down('#unique-calendar-name-field').setDisabled(true);

            me.down('#tou-campaign-allowed-comtask').setDisabled(true);
            me.down('#tou-campaign-send-connection-strategy-container').setDisabled(true);
        };

        me.setLoading(false);
        me.campaignRecordBeingEdited = campaignRecord;
        me.on('tou-deviceTypeChanged', setOptions);
        me.loadRecord(campaignRecord);
        DeviceGroupComboAndSetDeviceType();
    }
});
