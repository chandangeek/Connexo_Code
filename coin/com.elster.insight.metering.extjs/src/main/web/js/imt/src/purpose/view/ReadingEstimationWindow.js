/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.purpose.view.ReadingEstimationWindow', {
    extend: 'Ext.window.Window',
    alias: 'widget.reading-estimation-window',
    modal: true,
    title: Uni.I18n.translate('general.editWithEstimator', 'IMT', 'Edit with estimator'),
    bothSuspected: false,
    record: null,

    requires: [
        'Uni.util.FormErrorMessage',
        'Uni.property.form.Property',
        'Uni.view.readings.EstimationComment'
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
                    xtype: 'combobox',
                    itemId: 'estimator-field',
                    name: 'estimatorImpl',
                    fieldLabel: Uni.I18n.translate('estimationDevice.estimator', 'IMT', 'Estimator'),
                    required: true,
                    editable: false,
                    store: 'Imt.purpose.store.Estimators',
                    valueField: 'implementation',
                    displayField: 'displayName',
                    queryMode: 'local',
                    forceSelection: true,
                    emptyText: Uni.I18n.translate('general.selectAnEstimator', 'IMT', 'Select an estimator...'),
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
                    xtype: 'property-form',
                    itemId: 'property-form',
                    defaults: {
                        labelWidth: 200
                    }
                },
                {
                    xtype: 'checkboxfield',
                    fieldLabel: Uni.I18n.translate('general.projectedValue', 'IMT', 'Projected value'),
                    boxLabel: Uni.I18n.translate('general.markValuesAsProjected', 'IMT', 'Mark value(s) as projected'),
                    itemId: 'markProjected',
                    checked: false,
                    labelWidth: 200
                },
                {
                    xtype: 'estimation-comment',
                    keepOriginal: true
                },
                {
                    xtype: 'fieldcontainer',
                    fieldLabel: '&nbsp;',
                    margin: '20 0 0 0',
                    items: [
                        {
                            xtype: 'button',
                            itemId: 'estimate-reading-button',
                            text: Uni.I18n.translate('general.estimate', 'IMT', 'Estimate'),
                            ui: 'action'
                        },
                        {
                            xtype: 'button',
                            itemId: 'cancel-button',
                            text: Uni.I18n.translate('general.cancel', 'IMT', 'Cancel'),
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
    }
});