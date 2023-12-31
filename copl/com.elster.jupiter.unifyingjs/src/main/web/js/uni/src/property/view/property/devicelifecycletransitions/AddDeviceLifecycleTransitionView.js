/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.property.view.property.devicelifecycletransitions.AddDeviceLifecycleTransitionView', {
    extend: 'Ext.form.Panel',
    alias: 'widget.uni-add-device-lifecycle-transition-view',
    requires: ['Uni.property.model.PropertyDeviceLifecycleTransition',
        'Uni.property.store.PropertyDeviceLifecycleTransition'],
    router: null,
    store: 'Uni.property.store.PropertyDeviceLifecycleTransition',
    initComponent: function () {

    var me = this;

        me.items =[
        {
            ui: 'large',
            itemId: 'uni-add-device-lifecycle-transition-panel',
            title: Uni.I18n.translate('devicelifecycletransitions.addDeviceConfigurations', 'UNI', 'Add device lifecycle transitions'),
            margin: '0 0 0 20',
            items: [
                {
                    itemId: 'form-errors',
                    xtype: 'uni-form-error-message',
                    name: 'form-errors',
                    hidden: true,
                    width: 460
                },
                {
                    itemId: 'deviceType',
                    xtype: 'combobox',
                    name: 'deviceType',
                    fieldLabel: Uni.I18n.translate('devicelifecycletransition.deviceType', 'UNI', 'Device type'),
                    required: true,
                    labelWidth: 200,
                    width: 456,
                    queryMode: 'local',
                    displayField: 'name',
                    valueField: 'name',
                    editable: false,
                    msgTarget: 'under',
                    allowBlank: false,
                    store: 'Uni.property.store.PropertyDeviceLifecycleTransition',
                    listeners:{
                        select: function(combo, records){
                            var follow = me.down('#deviceLifecycle');
                            follow.getStore().loadRawData(records[0].get('values'));

                            me.down('#deviceLifecycle').setDisabled(false);
                            me.down('#transition').setDisabled(true);
                            me.down('#fromTransition').setDisabled(true);
                            me.down('#toTransition').setDisabled(true);

                            me.down('#deviceLifecycle').setValue('');
                            me.down('#transition').setValue('');
                            me.down('#fromTransition').setValue('');
                            me.down('#toTransition').setValue('');
                        }
                    }
                },
                {
                    itemId: 'deviceLifecycle',
                    xtype: 'combobox',
                    name: 'deviceLifecycle',
                    fieldLabel: Uni.I18n.translate('devicelifecycletransition.deviceLifecycle', 'UNI', 'Device Lifecycle'),
                    required: true,
                    store: Ext.create('Uni.property.store.PropertyDeviceLifecycleTransition'),
                    queryMode: 'local',
                    labelWidth: 200,
                    width: 456,
                    displayField: 'name',
                    valueField: 'name',
                    disabled: true,
                    editable: false,
                    allowBlank: false,
                    listeners:{
                        select: function(combo, records){
                            var follow = me.down('#transition');
                            follow.getStore().loadRawData(records[0].get('values'));
                            me.down('#transition').setDisabled(false);
                            me.down('#fromTransition').setDisabled(true);
                            me.down('#toTransition').setDisabled(true);

                            me.down('#transition').setValue('');
                            me.down('#fromTransition').setValue('');
                            me.down('#toTransition').setValue('');
                        }
                    }
                },
                {
                    itemId: 'transition',
                    xtype: 'combobox',
                    name: 'transition',
                    fieldLabel: Uni.I18n.translate('devicelifecycletransition.transition', 'UNI', 'Transition'),
                    required: true,
                    store: Ext.create('Uni.property.store.PropertyDeviceLifecycleTransition'),
                    queryMode: 'local',
                    labelWidth: 200,
                    width: 456,
                    displayField: 'name',
                    valueField: 'name',
                    disabled: true,
                    editable: false,
                    allowBlank: false,
                    listeners:{
                        select: function(combo, records){
                            var follow = me.down('#fromTransition');
                            follow.getStore().loadRawData(records[0].get('values'));
                            me.down('#fromTransition').setDisabled(false);
                            me.down('#toTransition').setDisabled(true);

                            me.down('#fromTransition').setValue('');
                            me.down('#toTransition').setValue('');
                        }
                    }
                },
                {
                    itemId: 'fromTransition',
                    xtype: 'combobox',
                    name: 'transition',
                    fieldLabel: Uni.I18n.translate('devicelifecycletransition.from', 'UNI', 'From'),
                    required: true,
                    store: Ext.create('Uni.property.store.PropertyDeviceLifecycleTransition'),
                    queryMode: 'local',
                    labelWidth: 200,
                    width: 456,
                    displayField: 'name',
                    valueField: 'name',
                    disabled: true,
                    editable: false,
                    allowBlank: false,
                    listeners:{
                        select: function(combo, records){
                            var follow = me.down('#toTransition');
                            follow.getStore().loadRawData(records[0].get('values'));
                            me.down('#toTransition').setDisabled(false);
                            me.down('#toTransition').setValue('');
                        }
                    }

                },
                {
                    itemId: 'toTransition',
                    xtype: 'combobox',
                    name: 'transition',
                    fieldLabel: Uni.I18n.translate('devicelifecycletransition.to', 'UNI', 'To'),
                    required: true,
                    store: Ext.create('Uni.property.store.PropertyDeviceLifecycleTransition'),
                    queryMode: 'local',
                    labelWidth: 200,
                    width: 456,
                    displayField: 'name',
                    valueField: 'name',
                    disabled: true,
                    editable: false,
                    allowBlank: false

                },
                {
                    xtype: 'fieldcontainer',
                    ui: 'actions',
                    fieldLabel: ' ',
                    defaultType: 'button',
                    items: [
                        {
                            itemId: 'addTransitionAction',
                            text:  Uni.I18n.translate('devicelifecycletransition.add', 'UNI', 'Add'),
                            ui: 'action',
                            action: 'add'
                        },
                        {
                            itemId: 'cancel',
                            text: Uni.I18n.translate('devicelifecycletransition.cancel', 'UNI', 'Cancel'),
                            ui: 'link'
                        }
                    ]
                }
            ]
        }
    ];




    me.callParent(arguments);
    }

});