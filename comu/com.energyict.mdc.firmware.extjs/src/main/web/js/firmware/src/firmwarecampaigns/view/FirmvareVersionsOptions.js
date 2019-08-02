/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Fwc.firmwarecampaigns.view.FirmvareVersionsOptions', {
    extend: 'Ext.form.FieldContainer',
    alias: 'widget.firmware-version-options',
    required: true,
    layout: {
        type: 'vbox',
        align: 'left'
    },
    store: 'Fwc.firmwarecampaigns.store.FirmvareVersionsOptions',
    isDependenciesSetted: false,

    initComponent: function () {
        var me = this;
        me.store = Ext.getStore(me.store) || Ext.create(me.store);

        me.items = [{
                xtype: 'checkboxgroup',
                required: false,
                itemId: 'firmwareTargetFileStatus',
                columns: 1,
                vertical: true,
                fieldLabel: Uni.I18n.translate('general.firmwareTargetFileStatus', 'FWC', 'Target firmware status'),
                name: 'targetFirmwareCheck',
                items: [
                    {
                        beforeSubTpl: '<span style="font-style:italic;color: grey;padding: 0 5px 5px 0;">' + Uni.I18n.translate('general.upload.fw.target.firm.status', 'FWC', 'Check if the uploaded firmware has this status') + '</span>',
                        itemId: 'targetFirmwareCheckFinal',
                        boxLabel: Uni.I18n.translate('general.upload.fw.targetFirmwareCheckFinalOption', 'FWC', 'Final status of target firmware'),
                        inputValue: 'FINAL',
                    },
                    {
                        itemId: 'targetFirmwareCheckTest',
                        boxLabel: Uni.I18n.translate('general.upload.fw.targetFirmwareCheckTestOption', 'FWC', 'Test status of target firmware'),
                        inputValue: 'TEST',
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
                xtype: 'checkboxgroup',
                fieldLabel: Uni.I18n.translate('general.firmwareDependenciesCheck', 'FWC', 'Dependencies check'),
                required: false,
                itemId: 'dependenciesCheckTargetOption',
                columns: 1,
                vertical: true,
                name: 'curFirmwareCheck',
                items: [
                    {
                        itemId: 'curFirmwareCheck',
                        boxLabel: '<b>' + Uni.I18n.translate('general.upload.fw.currentFirmwareCheck', 'FWC', 'The target firmware version should have a higher rank than the current firmware version on the device with the same type. All firmware types present in the device should have a rank not less than that of the version with the minimal level configured on the target version') + '</b>',
                        inputValue: 'COMMON'
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
                items: [
                    {
                        itemId: 'masterFirmwareCheck',
                        boxLabel: '<b>' + Uni.I18n.translate('general.upload.fw.masterFirmwareCheck', 'FWC', 'Master has the latest firmware (both meter and communication)') + '</b>',
                        inputValue: 'COMMON',
                        afterSubTpl: '<span style="font-style:italic;color: grey;padding: 0 0 0 19px;">' + Uni.I18n.translate('general.upload.fw.masterFirmwareCheck.comment', 'FWC', 'The latest firmeware on the master is chosen only within versions with the selected status') + '</span>',
                    }
                ]
            },
            {
                xtype: 'checkboxgroup',
                required: false,
                itemId: 'masterFirmwareCheckOptions',
                columns: 1,
                vertical: true,
                fieldLabel: ' ',
                margin: '0 0 0 30',
                name: 'masterFirmwareCheck',
                items: [
                    {
                        itemId: 'masterFirmwareCheckFinal',
                        boxLabel: Uni.I18n.translate('general.upload.fw.masterFirmwareCheckFinalOption', 'FWC', 'Final status of firmware on master device'),
                        inputValue: 'FINAL',
                    },
                    {
                        itemId: 'masterFirmwareCheckTest',
                        boxLabel: Uni.I18n.translate('general.upload.fw.targetFirmwareCheckTestOption', 'FWC', 'Test status of firmware on master device'),
                        inputValue: 'TEST',
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
                                    item.setValue(true);
                                    hasEnabledOption = true;
                                 }else{
                                    item.setValue(false);
                                 }
                             });
                             if ( key === 'masterFirmwareCheck' && !hasEnabledOption ) me.down('#masterFirmwareCheck').setValue(false);
                        }
                    }
                    if (!me.isDependenciesSetted){
                        me.setChecksDependencies('masterFirmwareCheck', 'masterFirmwareCheckFinal', 'masterFirmwareCheckTest', record.get('masterFirmwareCheck'));
                    }
                })

        },
        getDataFromChecks : function(){
            var me = this,
                store = me.store;
                var record = store.getAt(0);

                var checkgroups = Ext.ComponentQuery.query('firmware-version-options > checkboxgroup');
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

                return versionOptions;
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
