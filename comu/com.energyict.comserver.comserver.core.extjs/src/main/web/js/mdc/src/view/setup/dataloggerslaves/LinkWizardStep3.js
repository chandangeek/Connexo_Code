/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.dataloggerslaves.LinkWizardStep3', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.dataloggerslave-link-wizard-step3',
    ui: 'large',

    requires: [
        'Uni.util.FormErrorMessage',
        'Uni.util.FormEmptyMessage',
        'Mdc.model.Register'
    ],

    initComponent: function () {
        var me = this;

        me.items = [
            {
                itemId: 'mdc-dataloggerslave-link-wizard-step3-errors',
                xtype: 'uni-form-error-message',
                width: 700,
                hidden: true
            },
            {
                xtype: 'container',
                itemId: 'mdc-dataloggerslave-link-wizard-step3-container',
                fieldLabel: '',
                layout: {
                    type: 'vbox',
                    align: 'stretch'
                },
                items: [

                ]
            }
        ];

        me.callParent(arguments);
    },

    initialize: function(registerConfigRecords, dataLoggerRegisterRecords, previouslyMappedRegisters) {
        var me = this;
        if (me.rendered) {
            me.doInitialize(registerConfigRecords, dataLoggerRegisterRecords, previouslyMappedRegisters);
        } else {
            me.on('afterrender', function() {
                me.doInitialize(registerConfigRecords, dataLoggerRegisterRecords, previouslyMappedRegisters);
            }, me, {single:true});
        }
    },

    doInitialize: function(registerConfigRecords, dataLoggerRegisterRecords, previouslyMappedRegisters) {
        var me = this,
            counter = 0,
            form;

        me.down('#mdc-dataloggerslave-link-wizard-step3-container').removeAll();
        if (Ext.isEmpty(registerConfigRecords)) {
            Ext.suspendLayouts();
            me.down('#mdc-dataloggerslave-link-wizard-step3-container').add({
                xtype: 'uni-form-empty-message',
                text: Uni.I18n.translate('general.dataLoggerSlave.noRegisters', 'MDC', 'There are no registers on the data logger slave.')
            });
            Ext.resumeLayouts(true);
            me.doLayout();
        } else {
            Ext.Array.forEach(registerConfigRecords, function (registerConfigRecord) {
                counter++;
                Ext.suspendLayouts();
                me.down('#mdc-dataloggerslave-link-wizard-step3-container').add({
                    xtype: 'container',
                    width: 700,
                    itemId: 'mdc-step3-form-' + counter,
                    layout: {
                        type: 'vbox',
                        align: 'stretch'
                    },
                    defaults: {
                        labelWidth: 300
                    },
                    items: [
                        {
                            xtype: 'combobox',
                            listConfig: {
                                loadingText: null,
                                loadMask: false
                            },
                            editable: false,
                            forceSelection: true,
                            multiSelect: false,
                            queryMode: 'local',
                            fieldLabel: registerConfigRecord.get('registerTypeName'),
                            store: me.createRegisterStore(dataLoggerRegisterRecords),
                            emptyText: Uni.I18n.translate('general.registerCombo.emptyText', 'MDC', 'Select a register...'),
                            displayField: 'registerTypeName',
                            valueField: 'id',
                            msgTarget: 'under',
                            maxWidth: 700,
                            itemId: 'mdc-step3-register-combo-' + counter
                        }
                    ]
                });

                Ext.resumeLayouts(true);
                me.doLayout();
            }, me);
        }

        // 1. (Pre)select combo items according to previously made choices
        // 2. (Pre)select a combo item if it's the only one available
        var i, registerCombo;

        counter = 0;
        for (i=0; true; i++) {
            counter++;
            registerCombo = me.down('#mdc-step3-register-combo-' + counter);
            if (Ext.isEmpty(registerCombo)) {
                break;
            }
            if (previouslyMappedRegisters) {
                registerCombo.setValue(previouslyMappedRegisters[counter]);
            } else if (registerCombo.getStore().getCount()===1) {
                registerCombo.setValue(registerCombo.getStore().getAt(0).get('id'));
            }
        }
    },

    createRegisterStore: function(dataLoggerRegisterRecords) {
        var me = this,
            store = Ext.create('Ext.data.Store', {
                model: 'Mdc.model.Register',
                sorters: [{
                    property: 'registerTypeName',
                    direction: 'ASC'
                }],
                autoLoad: false
            });

        Ext.Array.forEach(dataLoggerRegisterRecords, function(registerRecord){
            store.add(registerRecord);
        }, me);
        return store;
    }

});