/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Tou.view.AddForm', {
    extend: 'Ext.form.Panel',
    requires: [
        'Uni.util.FormErrorMessage',
        'Uni.property.form.Property',
        'Uni.form.field.TimeInHoursAndMinutes',
        'Ext.form.RadioGroup',
        'Tou.view.ActivateCalendar',
        'Tou.store.DaysWeeksMonths'
    ],
    alias: 'widget.tou-campaigns-add-form',
    returnLink: null,
    action: null,
    skipLoadingIndication: false,
    campaignRecordBeingEdited: null,

    defaults: {
        labelWidth: 260,
        width: 800,
        msgTarget: 'under'
    },
    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'uni-form-error-message',
                itemId: 'form-errors',
                name: 'form-errors',
                margin: '0 0 10 0',
                hidden: true
            },
            {
                xtype: 'textfield',
                itemId: 'tou-campaign-name',
                name: 'name',
                fieldLabel: 'Name',
                required: true,
                allowBlank: false
            },
            {
                xtype: 'combobox',
                itemId: 'tou-campaign-device-type',
                name: 'deviceType',
                fieldLabel: 'Device type',
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
            },
            {
                xtype: 'fieldcontainer',
                fieldLabel: 'Device group',
                itemId: 'tou-campaign-device-group-field-container',
                required: true,
                allowBlank: false,
                layout: 'hbox',
                width: 650,
                items: [
                    {
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
            },
            {
                xtype: 'timeInHoursAndMinutes',
                fieldLabel: 'Time boundary start',
                name: 'activationStart',
                itemId: 'activationStart',
                required: true,
                allowBlank: false
            },
            {
                xtype: 'timeInHoursAndMinutes',
                fieldLabel: 'Time boundary end',
                name: 'activationEnd',
                itemId: 'activationEnd',
                required: true,
                allowBlank: false
            },
            {
                xtype: 'activate-calendar-field',
                itemId: 'activate-calendar',
                id: 'tou-activate-calendar',
                name: 'activation',
                fieldLabel: 'Activate calendar',
                required: true,
                allowBlank: false,
                hidden: true,
            },
            {
                xtype: 'radiogroup',
                itemId: 'tou-update-type',
                name: 'updateOption',
                fieldLabel: 'Update',
                required: true,
                hidden: true,
                vertical: true,
                columns: 1,
                renderTo: Ext.getBody(),
                items: [
                   {id : 'fullCalendar', boxLabel : 'Full calendar', inputValue: 'fullCalendar', checked: true, name : 'updateType'},
                   {id : 'specialDays', boxLabel : 'Only special days', inputValue: 'specialDays', name : 'updateType'}
                ],
                width: 1000
            },
            {
                itemId: 'tou-period-values',
                xtype: 'fieldcontainer',
                name: 'timeValidation',
                fieldLabel: 'Timeout before validation',
                margin: '30 0 10 0',
                required: true,
                layout: 'hbox',
                hidden: true,
                items: [
                    {
                        itemId: 'period-number',
                        xtype: 'numberfield',
                        name: 'recurrenceNumber',
                        allowDecimals: false,
                        valueField: 'id',
                        minValue: 1,
                        value: 1,
                        width: 65,
                        margin: '0 10 0 0'
                    },
                    {
                        itemId: 'period-combo',
                        xtype: 'combobox',
                        store: Ext.create('Tou.store.DaysWeeksMonths'),
                        displayField: 'displayValue',
                        valueField: 'id',
                        queryMode: 'local',
                        width: 100,
                        listeners: {
                            afterrender: function() {
                                  var defaultVal = this.store.getAt('1').get('name');
                                  //var periodCombo = this;
                                  //periodCombo.setRawValue(periodCombo.getStore().findRecord('name',validationTimeout.timeUnit).get('displayValue'));
                                  //periodNumber.setValue(validationTimeout.id);
                                  this.setRawValue(defaultVal);
                                  //this.select(defaultVal, true);
                                }
                        }
                    }
                ]
            },
            {
                   xtype: 'combobox',
                   itemId: 'tou-campaign-allowed-calendar',
                   name: 'сalendar',
                   fieldLabel: 'Tou calendar',
                   required: true,
                   allowBlank: false,
                   //store: 'Tou.store.AllowedCalendar',
                   forceSelection: true,
                   queryMode: 'local',
                   displayField: 'name',
                   valueField: 'id',
                   hidden: true
            },
            {
                xtype: 'fieldcontainer',
                itemId: 'form-buttons',
                fieldLabel: '&nbsp;',
                layout: 'hbox',
                margin: '20 0 0 0',
                items: [
                    {
                        xtype: 'button',
                        itemId: 'btn-add-tou-campaign',
                        text: 'Add',
                        ui: 'action',
                        action: me.action
                    },
                    {
                        xtype: 'button',
                        itemId: 'btn-cancel-add-tou-campaign',
                        text: 'Cancel',
                        ui: 'link',
                        action: 'cancel',
                        href: me.returnLink
                    }
                ]
            }
        ];

        me.callParent(arguments);
    },
    onDeviceTypeChange: function(radiogroup, newValue){
         var me = this;
         var activateCalendarItem = me.down('#activate-calendar');
         activateCalendarItem.show();
         me.down('#tou-update-type').show();
         me.down('#tou-period-values').show();
         var cbxCal = me.down('#tou-campaign-allowed-calendar');
         cbxCal.show();
         var calStore = Ext.create('Tou.store.AllowedDeviceTypeOptions');
         calStore.getProxy().setUrl(newValue);
         calStore.load(function(){
             var calParams = calStore.getAt(0);
             cbxCal.bindStore(calParams.calendars());

             if (calParams.get('fullCalendar')) {
                 Ext.getCmp("fullCalendar").enable();
             }else{
                 Ext.getCmp("fullCalendar").disable();
             }
             if (calParams.get('specialDays')){
                 Ext.getCmp("specialDays").enable();
             }else{
                  Ext.getCmp("specialDays").disable();
             }
             if (calParams.get('withActivationDate')){
                  Ext.getCmp("TouByDate").enable();
             }else{
                   Ext.getCmp("TouByDate").disable();
             }
             me.fireEvent('tou-deviceTypeChanged');
        });
    },
    loadRecordForEdit: function(campaignRecord) {
            var me = this,
                deviceTypeCombo = me.down('#tou-campaign-device-type'),
                deviceGroupComboContainer = me.down('#tou-campaign-device-group-field-container'),
                deviceGroupCombo = me.down('#tou-campaign-device-group'),
                periodCombo = me.down('#period-combo'),
                periodNumber = me.down('#period-number'),
                periodValues = me.down('#tou-period-values'),
                deviceTypeId = campaignRecord.get('deviceType').id,
                DeviceGroupComboAndSetDeviceType = function() {
                    deviceGroupCombo.setDisabled(true);
                    deviceGroupCombo.setRawValue(campaignRecord.get('deviceGroup'));
                    //deviceGroupComboContainer.allowBlank = true;
                    //deviceGroupComboContainer.hide();
                    deviceTypeCombo.setDisabled(true);
                    deviceTypeCombo.setValue(deviceTypeId);
                },
                setTimeoutFld = function(validationTimeout){
                    if(validationTimeout){
                        /*periodCombo.setRawValue(periodCombo
                            .getStore().findRecord('name',validationTimeout.timeUnit).get('displayValue'));*/
                        var timeUnit = 'minutes';
                        validationTimeout = validationTimeout/60;
                        if (validationTimeout % 60){
                            validationTimeout = validationTimeout/60;
                            timeUnit = 'hours';
                        }
                        if (validationTimeout % 24){
                            validationTimeout = validationTimeout/24;
                            timeUnit = 'days';
                        }
                        if (validationTimeout % 7){
                            validationTimeout = validationTimeout/7;
                            timeUnit = 'weeks';
                        }
                        periodNumber.setValue(validationTimeout);
                        periodCombo.setRawValue(timeUnit);
                    }else{
                        periodNumber.setValue(0);
                        periodCombo.setRawValue('minutes');
                    }
                },
                setOptions = function() {
                    periodValues.setDisabled(true);
                    setTimeoutFld(campaignRecord.get('timeValidation'));
                    var timeStartInSec = new Date(campaignRecord.get('activationStart')/1000);
                    var timeEndInSec = new Date(campaignRecord.get('activationEndt')/1000);
                    me.down("#activationStart").setValue(timeStartInSec.getHours() *3600 + timeStartInSec.getMinutes() *60);
                    me.down("#activationEnd").setValue(timeEndInSec.getHours() *3600 + timeEndInSec.getMinutes() *60);
                    //me.down("#activate-calendar").setValue({"activationOption" : campaignRecord.get('activationOption'), "activationDate" : campaignRecord.get('activationDate')})
                    me.down("#activate-calendar").setDisabled(true);
                    me.down("#tou-campaign-allowed-calendar").setValue(campaignRecord.get('calendar').id);
                    me.down("#tou-campaign-allowed-calendar").setDisabled(true);
                    me.down("#tou-update-type").setValue(campaignRecord.get('updateType'));
                    me.down("#tou-update-type").setDisabled(true);

                };
                me.setLoading(false);
                me.campaignRecordBeingEdited = campaignRecord;
                me.on('tou-deviceTypeChanged', setOptions);
                me.loadRecord(campaignRecord);
                DeviceGroupComboAndSetDeviceType();
        }
});