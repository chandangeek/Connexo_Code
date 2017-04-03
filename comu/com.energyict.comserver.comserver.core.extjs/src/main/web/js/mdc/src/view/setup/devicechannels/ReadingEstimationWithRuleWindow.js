/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.devicechannels.ReadingEstimationWithRuleWindow', {
    extend: 'Ext.window.Window',
    alias: 'widget.reading-estimation-with-rule-window',
    modal: true,
    title: Uni.I18n.translate('general.EstimateWithRule', 'MDC', 'Estimate with rule'),
    bothSuspected: false,
    record: null,
    hasRules: false,

    requires: [
        'Uni.util.FormErrorMessage',
        'Uni.property.form.Property'
    ],

    initComponent: function () {
        var me = this;

        me.items = {
            xtype: 'form',
            itemId: 'reading-estimation-window-form',
            padding: 0,
            defaults: {
                width: 503,
                labelWidth: 200
            },
            items: [
                {
                    xtype: 'uni-form-error-message',
                    itemId: 'form-errors',
                    hidden: true
                },
                {
                    xtype: 'label',
                    itemId: 'error-label',
                    hidden: true,
                    margin: '10 0 10 20'
                },
                {
                    xtype: 'radiogroup',
                    fieldLabel: Uni.I18n.translate('general.valueToEstimate', 'MDC', 'Value to estimate'),
                    itemId: 'value-to-estimate-radio-group',
                    required: true,
                    columns: 1,
                    vertical: true,
                    hidden: !me.bothSuspected,
                    defaults: {
                        name: 'isBulk'
                    },
                    items: [
                        {
                            itemId: 'rbtn-is-bulk-no',
                            boxLabel: Uni.I18n.translate('general.valueWh', 'MDC', 'Value (Wh)'),
                            inputValue: false,
                            checked: true
                        },
                        {
                            itemId: 'rbtn-is-bulk-yes',
                            boxLabel: Uni.I18n.translate('general.bulkValueWh', 'MDC', 'Bulk value (Wh)'),
                            inputValue: true
                        }
                    ]
                },
                {
                    xtype: 'fieldcontainer',
                    fieldLabel: Uni.I18n.translate('estimationDevice.estimationRule', 'MDC', 'Estimation rule'),
                    itemId: 'estimator-container',
                    required: true,
                    items: [
                        {
                            xtype: 'combobox',
                            itemId: 'estimator-field',
                            width: 280,
                            name: 'estimatorImpl',
                            editable: false,
                            store: 'Mdc.store.EstimationRulesOnChannelMainValue',
                            valueField: 'id',
                            displayField: 'name',
                            queryMode: 'local',
                            forceSelection: true,
                            hidden: !me.hasRules,
                            emptyText: Uni.I18n.translate('general.selectAnEstimationRule', 'MDC', 'Select an estimation rule...'),
                            listeners: {
                                change: {
                                    fn: function (implementationCombo, newValue) {
                                        var estimator = implementationCombo.getStore().getById(newValue);

                                        estimator && me.down('property-form').loadRecord(estimator);
                                        me.updateLayout();
                                        me.center();
                                    }
                                }
                            }
                        },
                        {
                            xtype: 'component',
                            html: Uni.I18n.translate('noEstimationRules.message', 'MDC', 'No applicable estimation rules'),
                            itemId: 'no-estimation-rules-component',
                            hidden: me.hasRules,
                            style: {
                                'color': '#FF0000',
                                'margin': '6px 0px 6px 0px'
                            }
                        }
                    ]
                },
                {
                    xtype: 'property-form',
                    itemId: 'property-form',
                    isEdit: false,
                    isReadOnly: true,
                    defaults: {
                        labelWidth: 200
                    }
                },
                {
                    xtype: 'fieldcontainer',
                    fieldLabel: '&nbsp;',
                    margin: '20 0 0 0',
                    items: [
                        {
                            xtype: 'button',
                            itemId: 'estimate-reading-button',
                            text: Uni.I18n.translate('general.estimate', 'MDC', 'Estimate'),
                            ui: 'action'
                        },
                        {
                            xtype: 'button',
                            itemId: 'cancel-button',
                            text: Uni.I18n.translate('general.cancel', 'MDC', 'Cancel'),
                            ui: 'link',
                            handler: function () {
                                me.close();
                            }
                        }
                    ]
                }
            ]
        };

        me.callParent(arguments);
    },

    getEstimator: function(){
        var me = this,
            record = me.down('#estimator-field').getStore().getById(me.down('#estimator-field').getValue());
        return record ? record.get('estimatorImpl') : '';
    }
});