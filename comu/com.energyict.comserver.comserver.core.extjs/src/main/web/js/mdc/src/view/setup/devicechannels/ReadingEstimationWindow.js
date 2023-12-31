/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.devicechannels.ReadingEstimationWindow', {
    extend: 'Ext.window.Window',
    alias: 'widget.reading-estimation-window',
    modal: true,
    shrinkWrapDock: true,
    title: Uni.I18n.translate('general.editWithEstimator', 'MDC', 'Edit with estimator'),
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
                            boxLabel: Uni.I18n.translate('general.valueWithUnit', 'MDC', 'Value ({0})', Ext.isDefined(me.currentChannel) ? me.currentChannel.get('readingType').names.unitOfMeasure : ''),
                            inputValue: false,
                            checked: true
                        },
                        {
                            itemId: 'rbtn-is-bulk-yes',
                            boxLabel: Uni.I18n.translate('general.bulkValueWithUnit', 'MDC', 'Bulk value ({0})',
                                Ext.isDefined(me.currentChannel) ?
                                    me.currentChannel.get('calculatedReadingType') ? me.currentChannel.get('calculatedReadingType').names.unitOfMeasure : ''
                                    : ''),
                            inputValue: true
                        }
                    ]
                },
                {
                    xtype: 'combobox',
                    itemId: 'estimator-field',
                    name: 'estimatorImpl',
                    fieldLabel: Uni.I18n.translate('estimationDevice.estimator', 'MDC', 'Estimator'),
                    required: true,
                    editable: false,
                    store: 'Mdc.store.Estimators',
                    valueField: 'implementation',
                    displayField: 'displayName',
                    queryMode: 'local',
                    forceSelection: true,
                    emptyText: Uni.I18n.translate('general.selectAnEstimator', 'MDC', 'Select an estimator...'),
                    listeners: {
                        change: {
                            fn: function (implementationCombo, newValue) {
                                var estimator = implementationCombo.getStore().getById(newValue);

                                if (newValue) {
                                    this.nextSibling('estimation-comment').show();
                                }
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
                    xtype: 'estimation-comment',
                    keepOriginal: true,
                    hidden: true
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
    }
});