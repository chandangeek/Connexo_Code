Ext.define('Mdc.view.setup.dataloggerslaves.LinkWizardStep3', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.dataloggerslave-link-wizard-step3',
    ui: 'large',

    requires: [
        'Uni.util.FormErrorMessage',
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

    initialize: function(registerConfigRecords, dataLoggerRegisterRecords) {
        var me = this;
        if (me.rendered) {
            me.doInitialize(registerConfigRecords, dataLoggerRegisterRecords);
        } else {
            me.on('afterrender', function() {
                me.doInitialize(registerConfigRecords, dataLoggerRegisterRecords);
            }, me, {single:true});
        }
    },

    doInitialize: function(registerConfigRecords, dataLoggerRegisterRecords) {
        var me = this,
            counter = 0,
            form;

        me.down('#mdc-dataloggerslave-link-wizard-step3-container').removeAll();
        Ext.Array.forEach(registerConfigRecords, function(registerConfigRecord) {
            counter++;
            Ext.suspendLayouts();
            me.down('#mdc-dataloggerslave-link-wizard-step3-container').add({
                xtype: 'container',
                width: 700,
                margin: '20 0 0 0',
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
            form = me.down('#mdc-step3-form-' + counter);

            if (registerConfigRecord.get('useMultiplier')) {
                form.add(
                    {
                        xtype: 'numberfield',
                        minValue: 1,
                        maxValue: 2147483647,
                        fieldLabel: Uni.I18n.translate('general.multiplier', 'MDC', 'Multiplier'),
                        value: 1,
                        maxWidth: 375
                    }
                );
            } else {
                form.add(
                    {
                        xtype: 'displayfield',
                        fieldLabel: Uni.I18n.translate('general.multiplier', 'MDC', 'Multiplier'),
                        value: Uni.I18n.translate('general.registerDoesntUseMultiplier', 'MDC', "Register doesn't use multiplier")
                    }
                );
            }

        }, me);
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