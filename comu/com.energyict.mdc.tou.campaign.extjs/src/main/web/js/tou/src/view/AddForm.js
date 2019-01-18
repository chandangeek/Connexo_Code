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
                    change: function(radiogroup, newValue){
                        var activateCalendarItem = me.down('#activate-calendar');
                        activateCalendarItem.show();
                        me.down('#tou-update-type').show();
                        me.down('#tou-period-values').show();
                        var cbxCal = me.down('#tou-campaign-allowed-calendar');
                        cbxCal.show();
                        var calStore = Ext.create('Tou.store.AllowedDeviceTypeOptions');
                        calStore.getProxy().setUrl(this.getValue());
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
                        });
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
                                  this.setValue(defaultVal);
                                  this.select(defaultVal, true);
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
    loadRecordForEdit: function(campaignRecord) {
            var me = this,
                taskRunner = new Ext.util.TaskRunner(),
                deviceTypeCombo = me.down('#tou-campaign-device-type'),
                //touTypeRadioGroup = me.down('#tou-type'),
                deviceGroupComboContainer = me.down('#tou-campaign-device-group-field-container'),
                deviceGroupCombo = me.down('#tou-campaign-device-group'),
                managementOptionRadioGroup = me.down('#tou-management-option'),
                periodCombo = me.down('#period-combo'),
                periodNumber = me.down('#period-number'),
                periodValues = me.down('#period-values'),
                deviceTypeId = campaignRecord.get('deviceType').id
        }
});