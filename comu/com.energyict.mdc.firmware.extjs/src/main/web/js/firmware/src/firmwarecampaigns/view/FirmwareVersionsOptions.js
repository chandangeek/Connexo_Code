/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Fwc.firmwarecampaigns.view.FirmwareVersionsOptions', {
    extend: 'Ext.form.FieldContainer',
    alias: 'widget.firmware-version-options',
    required: true,
    layout: {
        type: 'vbox',
        align: 'left'
    },
    store: 'Fwc.firmwarecampaigns.store.FirmwareVersionsOptions',
    isDependenciesSetted: false,

    initComponent: function () {
        var me = this;
        me.store = Ext.getStore(me.store) || Ext.create(me.store);

        me.items = [
            {
                xtype: 'displayfield',
                itemId: 'firmwareDeviceTypeVersionsWarn',
                fieldLabel: '&nbsp',
                width: 800,
                renderer: function (value, field) {
                    return '<span style="color:#808080;">' + Uni.I18n.translate('firmware.device.type.versions.warn', 'FWC', 'Default values for the following fields are taken from the device type. Firmware checks will be used on the campaign as specified below') + '</span>';
                }
            },
            {
                xtype: 'fieldcontainer',
                itemId: 'targetOptions',
                layout: 'hbox',
                flex: 1,
                width: 1000,
                required: true,
                fieldLabel: Uni.I18n.translate('general.targetManagementOptions', 'FWC', 'Target firmware status'),
                items: [
                    {
                        xtype: 'checkboxgroup',
                        required: false,
                        itemId: 'firmwareTargetFileStatus',
                        columns: 1,
                        vertical: true,
                        width: 200,
                        name: 'targetFirmwareCheck',
                        items: [
                            {
                                beforeSubTpl: '<span style="font-style:italic;color: grey;padding: 0 5px 15px 0;">' + Uni.I18n.translate('general.upload.fw.target.firm.status', 'FWC', 'Check if the uploaded firmware has this status') + '</span>',
                                itemId: 'targetFirmwareCheckFinal',
                                boxLabel: Uni.I18n.translate('general.targetFirmwareFinalOption', 'FWC', 'Final status of target firmware'),
                                inputValue: 'FINAL',
                                margin: '10 0',
                                listeners: {
                                    change: function(checkBox, value) {
                                        if (this.originalValue !== value) {
                                            me.down('#targetFirmwareCheckFinalReset').enable();
                                        } else me.down('#targetFirmwareCheckFinalReset').disable();
                                    }
                                }
                            },
                            {
                                itemId: 'targetFirmwareCheckTest',
                                boxLabel: Uni.I18n.translate('general.targetFirmwareTestOption', 'FWC', 'Test status of target firmware'),
                                inputValue: 'TEST',
                                margin: '10 0',
                                listeners: {
                                    change: function(checkBox, value) {
                                        if (this.originalValue !== value) {
                                             me.down('#targetFirmwareCheckTestReset').enable();
                                        } else me.down('#targetFirmwareCheckTestReset').disable();
                                    }
                                }
                            }
                        ],
                    },
                    {
                        xtype: 'fieldcontainer',
                        itemId: 'resetBtns',
                        layout: {
                            type: 'vbox',
                        },
                        fieldLabel: '',
                        margin: '45 0 0 0',
                        items: [
                            {
                                xtype: 'uni-default-button',
                                itemId: 'targetFirmwareCheckFinalReset',
                                hidden: false,
                                disabled: true,
                                handler: function(){
                                    me.down('#targetFirmwareCheckFinal').reset();
                                    this.disable();
                                },
                                listeners: {
                                    afterrender: function(){
                                        me.on('dependenciesSetted', function(){
                                           var targetFirmwareCheckFinalValue = me.down('#targetFirmwareCheckFinal') && me.down('#targetFirmwareCheckFinal').originalValue;
                                           me.down('#targetFirmwareCheckFinalReset').setTooltip(Uni.I18n.translate('general.restoreDefaultValue', 'FWC', 'Restore to default value') + ' "' + Boolean(targetFirmwareCheckFinalValue) + '"');

                                        })
                                    }
                                }
                            },
                            {
                                xtype: 'uni-default-button',
                                itemId: 'targetFirmwareCheckTestReset',
                                hidden: false,
                                disabled: true,
                                handler: function(){
                                    me.down('#targetFirmwareCheckTest').reset();
                                    this.disable();
                                },
                                listeners: {
                                    afterrender: function(){
                                        me.on('dependenciesSetted', function(){
                                           var targetFirmwareCheckTestValue = me.down('#targetFirmwareCheckTest') && me.down('#targetFirmwareCheckTest').originalValue;
                                           me.down('#targetFirmwareCheckTestReset').setTooltip(Uni.I18n.translate('general.restoreDefaultValue', 'FWC', 'Restore to default value') + ' "' + Boolean(targetFirmwareCheckTestValue) + '"');

                                        })
                                    }
                                }
                            }
                        ]
                    }
                ]

            },
            {
                xtype: 'displayfield',
                itemId: 'firmwareTargetOptionsError',
                padding: '-10 0 -10 0',
                fieldLabel: '&nbsp',
                hidden: true,
                renderer: function (value, field) {
                    return '<span style="color:red;">' + Uni.I18n.translate('firmware.specs.save.validationError', 'FWC', 'You must select at least one item in the group') + '</span>';
                }
            },
            {
                xtype: 'fieldcontainer',
                itemId: 'curOptions',
                layout: 'hbox',
                flex: 1,
                width: 750,
                fieldLabel: Uni.I18n.translate('general.rankManagementOptions', 'FWC', 'Dependencies check'),
                items: [
                     {
                        xtype: 'checkboxgroup',
                        required: false,
                        itemId: 'dependenciesCheckTargetOption',
                        columns: 1,
                        vertical: true,
                        name: 'curFirmwareCheck',
                        fieldLabel:'',
                        width: 400,
                        items: [
                            {
                                itemId: 'curFirmwareCheck',
                                boxLabel: '<b>' + Uni.I18n.translate('general.upload.fw.currentFirmwareCheck', 'FWC', 'The target firmware version should have a higher rank than the current firmware version on the device with the same type') + '</b>',
                                inputValue: 'COMMON',
                                margin: '10 0',
                                listeners: {
                                    change: function(checkBox, value) {
                                        if (this.originalValue !== value) {
                                             me.down('#curFirmwareCheckReset').enable();
                                        } else me.down('#curFirmwareCheckReset').disable();
                                    }
                                }
                            }
                        ]
                    },
                    {
                        xtype: 'fieldcontainer',
                        itemId: 'curFirmwareCheckResetBtns',
                        layout: {
                            type: 'vbox',
                        },
                        fieldLabel: '',
                        margin: '25 0 0 0',
                        items: [
                            {
                                xtype: 'uni-default-button',
                                itemId: 'curFirmwareCheckReset',
                                hidden: false,
                                disabled: true,
                                handler: function(){
                                     me.down('#curFirmwareCheck').reset();
                                     this.disable();
                                },
                                listeners: {
                                    afterrender: function(){
                                        me.on('dependenciesSetted', function(){
                                           var curFirmwareCheckValue = me.down('#curFirmwareCheck') && me.down('#curFirmwareCheck').originalValue;
                                           me.down('#curFirmwareCheckReset').setTooltip(Uni.I18n.translate('general.restoreDefaultValue', 'FWC', 'Restore to default value') + ' "' + Boolean(curFirmwareCheckValue) + '"');

                                        })
                                    }
                                }
                            }
                        ]
                    }
                ]
            },
            {
                xtype: 'checkboxgroup',
                fieldLabel: ' ',
                required: false,
                itemId: 'masterFirmwareMainOption',
                columns: 1,
                vertical: true,
                name: 'masterFirmwareWasSelectedCheck',
                width: 800,
                items: [
                    {
                        itemId: 'masterFirmwareCheck',
                        boxLabel: '<b>' + Uni.I18n.translate('general.upload.fw.masterFirmwareCheck', 'FWC', 'Master has the latest firmware (meter, communication and auxiliary)') + '</b>',
                        inputValue: 'COMMON',
                        afterSubTpl: '<span style="font-style:italic;color: grey;padding: 0 0 0 19px;">' + Uni.I18n.translate('general.upload.fw.masterFirmwareCheck.comment', 'FWC', 'The latest firmware on the master is chosen only within versions with the selected status') + '</span>',
                    }
                ]
            },
            {
                xtype: 'fieldcontainer',
                itemId: 'masterOptions',
                layout: 'hbox',
                flex: 1,
                width: 800,
                fieldLabel: ' ',
                items: [
                    {
                        xtype: 'checkboxgroup',
                        required: false,
                        itemId: 'masterFirmwareCheckOptions',
                        columns: 1,
                        vertical: true,
                        fieldLabel: '',
                        margin: '0 0 0 30',
                        name: 'masterFirmwareCheck',
                        width: 300,
                        items: [
                            {
                                itemId: 'masterFirmwareCheckFinal',
                                boxLabel: Uni.I18n.translate('general.upload.fw.masterFirmwareCheckFinalOption', 'FWC', 'Final status of firmware on master device'),
                                inputValue: 'FINAL',
                                margin: '10 0',
                                listeners: {
                                    change: function(checkBox, value) {
                                        if (this.originalValue !== value) {
                                            me.down('#masterFirmwareCheckFinalReset').enable();
                                        } else {
                                            me.down('#masterFirmwareCheckFinalReset').disable();

                                            if (!me.down('#masterFirmwareCheckTest').getValue()) {
                                                me.down('#masterFirmwareMainOption').setValue(false);
                                             }
                                        }
                                    }
                                }
                            },
                            {
                                itemId: 'masterFirmwareCheckTest',
                                boxLabel: Uni.I18n.translate('general.upload.fw.masterFirmwareCheckTestOption', 'FWC', 'Test status of firmware on master device'),
                                inputValue: 'TEST',
                                margin: '10 0',
                                listeners: {
                                    change: function(checkBox, value) {
                                        if (this.originalValue !== value) {
                                            me.down('#masterFirmwareCheckTestReset').enable();
                                        } else {
                                            me.down('#masterFirmwareCheckTestReset').disable();

                                            if (!me.down('#masterFirmwareCheckFinal').getValue()) {
                                                me.down('#masterFirmwareMainOption').setValue(false);
                                            }
                                        }
                                    }
                                }
                            }
                        ]
                    },
                    {
                        xtype: 'fieldcontainer',
                        itemId: 'resetBtns',
                        layout: {
                            type: 'vbox',
                        },
                        fieldLabel: '',
                        margin: '10 0 0 0',
                        items: [
                            {
                                xtype: 'uni-default-button',
                                itemId: 'masterFirmwareCheckFinalReset',
                                hidden: false,
                                disabled: true,
                                handler: function(){
                                    me.down('#masterFirmwareCheckFinal').reset();
                                    this.disable();
                                    if (!me.down('#masterFirmwareCheckFinal').getValue() && !me.down('#masterFirmwareCheckTest').getValue()) me.down('#masterFirmwareMainOption').setValue(false);
                                },
                                listeners: {
                                    afterrender: function(){
                                        me.on('dependenciesSetted', function(){
                                           var masterFirmwareCheckFinalValue = me.down('#masterFirmwareCheckFinal') && me.down('#masterFirmwareCheckFinal').originalValue;
                                           me.down('#masterFirmwareCheckFinalReset').setTooltip(Uni.I18n.translate('general.restoreDefaultValue', 'FWC', 'Restore to default value') + ' "' + Boolean(masterFirmwareCheckFinalValue) + '"');

                                        })
                                    }
                                }
                            },
                            {
                                xtype: 'uni-default-button',
                                itemId: 'masterFirmwareCheckTestReset',
                                hidden: false,
                                disabled: true,
                                handler: function(){
                                    me.down('#masterFirmwareCheckTest').reset();
                                    this.disable();
                                    if (!me.down('#masterFirmwareCheckTest').getValue()){
                                         if (!me.down('#masterFirmwareCheckFinal').getValue()){
                                             me.down('#masterFirmwareMainOption').setValue(false);
                                         }
                                    }
                                    else{
                                         var masterFirmwareCheckFinal = me.down('#masterFirmwareCheckFinal').getValue();
                                         me.down('#masterFirmwareCheck').setValue(true);
                                         me.down('#masterFirmwareCheckFinal').setValue(masterFirmwareCheckFinal);
                                    }
                                },
                                listeners: {
                                    afterrender: function(){
                                        me.on('dependenciesSetted', function(){
                                           var masterFirmwareCheckTestValue = me.down('#masterFirmwareCheckTest') && me.down('#masterFirmwareCheckTest').originalValue;
                                           me.down('#masterFirmwareCheckTestReset').setTooltip(Uni.I18n.translate('general.restoreDefaultValue', 'FWC', 'Restore to default value') + ' "' + Boolean(masterFirmwareCheckTestValue) + '"');

                                        })
                                    }
                                }
                            }
                        ]
                    }
                ]
            },
            {
                xtype: 'displayfield',
                itemId: 'masterOptionsError',
                fieldLabel: '&nbsp',
                hidden: true,
                renderer: function (value, field) {
                    return '<span style="color:red;margin:10px 0 0 30px;">' + Uni.I18n.translate('firmware.specs.save.validationError', 'FWC', 'You must select at least one item in the group') + '</span>';
                }
            }]

            me.callParent(arguments);
        },
        fillChecksAccordingStore : function(){
            var me = this,
                store = me.store;
                store.each(function(record){

                    var data = record.getData();
                    for (var key in data){
                        var checkgroup;
                        var hasEnabledOption = false;
                        if ( key !== 'id' && ( checkgroup = me.down('[name=' + key + ']') )){
                             checkgroup.items.each(function(item){
                                 if ( data[key].indexOf(item.inputValue) !== -1 ){
                                    item.originalValue = true;
                                    item.setValue(true);
                                    hasEnabledOption = true;
                                 }else{
                                    item.originalValue = false;
                                    item.setValue(false);
                                 }
                             });
                             if ( key === 'masterFirmwareCheck' && !hasEnabledOption ) me.down('#masterFirmwareCheck').setValue(false);
                        }
                    }
                    if (!me.isDependenciesSetted){
                        me.setChecksDependencies('masterFirmwareCheck', 'masterFirmwareCheckFinal', 'masterFirmwareCheckTest', record.get('masterFirmwareCheck'));
                    }
                    me.fireEvent('dependenciesSetted');
                })

        },
        validateData: function(versionOptions){

            var me = this;
            var result = versionOptions;
            var masterOptions = versionOptions && versionOptions['MASTER_FIRMWARE_CHECK'] && versionOptions['MASTER_FIRMWARE_CHECK'].statuses;
            var targetOptions = versionOptions && versionOptions['TARGET_FIRMWARE_STATUS_CHECK'] && versionOptions['TARGET_FIRMWARE_STATUS_CHECK'].statuses;

            if (!targetOptions.length){
                me.down('#firmwareTargetOptionsError').show();
                result = undefined;
            }else{
                me.down('#firmwareTargetOptionsError').hide();
            }

            if (me.down('#masterFirmwareCheck').getValue() && (!masterOptions || !masterOptions.length)){
                  me.down('#masterOptionsError').show();
                  result = undefined;
            }else{
                  me.down('#masterOptionsError').hide();
            }

            return result;
        },
        getDataFromChecks : function(needDataValidation){
            var me = this,
                store = me.store;
                var record = store.getAt(0);

                var checkgroups = Ext.ComponentQuery.query('firmware-version-options checkboxgroup');
                var versionOptions = {};

                Ext.Array.each( checkgroups, function(checkgroup){
                    var values = checkgroup.getValues();
                    switch (checkgroup.getName()){
                        case 'targetFirmwareCheck':
                           var statuses = Ext.Object.getValues(values);
                           versionOptions['TARGET_FIRMWARE_STATUS_CHECK'] = {
                                'statuses' : statuses,
                                'activated' : Boolean(statuses && statuses.length)
                           }
                        break;
                        case 'masterFirmwareCheck':
                           var statuses = Ext.Object.getValues(values);
                           versionOptions['MASTER_FIRMWARE_CHECK'] = {
                                'statuses' : statuses,
                                'activated' : Boolean(statuses && statuses.length)
                           }
                        break;
                        case 'curFirmwareCheck':
                           var statuses = Ext.Object.getValues(values);
                           versionOptions['CURRENT_FIRMWARE_CHECK'] = {
                                'activated' : Boolean(statuses && statuses.length)
                           }
                        break;

                    }
                });
                if (needDataValidation){
                    return me.validateData(versionOptions);
                } else {
                    return versionOptions;
                }
        },
        setChecksDependencies: function (mainOptionId, finalOptionId, testOptionId, modelData){
            var me = this;
             var mainOption =  me.down('#' + mainOptionId);
             var finalOption =  me.down('#' + finalOptionId);
             var testOption =  me.down('#' + testOptionId);

             if (modelData){
                  mainOption.show();

                 if ( modelData && (modelData instanceof Array) ){

                      finalOptionVal = modelData.indexOf('FINAL') !==-1;
                      testOptionVal = modelData.indexOf('TEST') !==-1;
                      if (finalOptionVal || testOptionVal){
                            mainOption.setValue(true);
                      }else{
                            finalOption.disable();
                            testOption.disable();
                      }
                      finalOption.setValue(finalOptionVal);
                      testOption.setValue(testOptionVal);

                 }

                 finalOption.on('change', function(checkBox, newVal, oldVal){
                      if (newVal === oldVal) return;
                      if (newVal) mainOption.setValue(newVal);
                 });

                 mainOption.on('change', function(checkBox, newVal, oldVal){
                      if (newVal === oldVal) return;
                      if (newVal) {  finalOption.enable(); finalOption.setValue(true); testOption.enable(); }
                      else { finalOption.setValue(false); finalOption.disable(); testOption.setValue(false); testOption.disable();}
                 });
                 me.isDependenciesSetted = true;

             }
        }

    });
