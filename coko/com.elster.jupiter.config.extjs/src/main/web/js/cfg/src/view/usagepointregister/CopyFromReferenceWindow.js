/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.view.usagepointregister.CopyFromReferenceWindow', {
    extend: 'Ext.window.Window',
    alias: 'widget.reading-copy-from-reference-window',
    modal: true,
    hideCollapseTool: true,
    title: Uni.I18n.translate('general.copyFromReference', 'CFG', 'Copy from reference'),
    record: null,
    usagePoint: null,

    store: {
        fields: [
            'referenceDevice',
            'referenceUsagePoint',
            'metrologyPurpose',
            'readingType',
            'startDate',
            'allowSuspectData',
            'completePeriod'
        ]
    },

    requires: [
        'Uni.util.FormErrorMessage',
        'Uni.property.form.Property'
    ],

    initComponent: function () {
        var me = this;

        me.items = {
            xtype: 'form',
            itemId: 'reading-estimation-window-form',
            padding: 5,
            defaults: {
                width: 510,
                labelWidth: 200,
                margin: 20
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
                    itemId: 'device-field',
                    dataIndex: 'referenceDevice',
                    fieldLabel: Uni.I18n.translate('copyFromReference.device', 'CFG', 'Check device'),
                    required: true,
                    width: '100%',
                    name: 'device',
                    store: 'Cfg.store.AllDevices',
                    valueField: 'id',
                    displayField: 'name',
                    queryMode: 'remote',
                    remoteFilter: true,
                    queryParam: 'name',
                    queryCaching: false,
                    minChars: 1,
                    editable: true,
                    typeAhead: true,
                    emptyText: Uni.I18n.translate('copyFromReference.selectDevice', 'CFG', 'Select device'),
                    // listeners: {
                    //     change: {
                    //         fn: function (implementationCombo, newValue) {
                    //             var estimator = implementationCombo.getStore().getById(newValue);
                    //
                    //             estimator && me.down('property-form').loadRecord(estimator);
                    //             me.updateLayout();
                    //             me.center();
                    //         }
                    //     }
                    // }
                },
                {
                    xtype: 'combobox',
                    itemId: 'readingType-field',
                    dataIndex: 'readingType',
                    fieldLabel: Uni.I18n.translate('copyFromReference.readingType', 'CFG', 'Check reading type'),
                    required: true,
                    width: '100%',
                    name: 'readingType',
                    store: 'Cfg.store.AllReadingTypes',
                    valueField: 'mRID',
                    displayField: 'aliasName',
                    queryMode: 'remote',
                    remoteFilter: true,
                    queryParam: 'value',
                    queryCaching: false,
                    minChars: 1,
                    editable: true,
                    typeAhead: true,
                    emptyText: Uni.I18n.translate('copyFromReference.selectReadingType', 'CFG', 'Select reading type'),
                    // listeners: {
                    //     change: {
                    //         fn: function (implementationCombo, newValue) {
                    //             var estimator = implementationCombo.getStore().getById(newValue);
                    //
                    //             estimator && me.down('property-form').loadRecord(estimator);
                    //             me.updateLayout();
                    //             me.center();
                    //         }
                    //     }
                    // }
                },
                {
                    xtype: 'fieldcontainer',
                    itemId: 'start-date-container',
                    dataIndex: 'startDate',
                    fieldLabel: Uni.I18n.translate('copyFromReference.startDate', 'CFG', 'Start date/time'),
                    defaults: {
                        width: '100%'
                    },
                    items: [
                        {
                            xtype: 'date-time',
                            itemId: 'start-date-field',
                            value: new Date(),
                            layout: 'hbox',
                            valueInMilliseconds: true
                        }
                    ]
                },
                {
                    xtype: 'checkbox',
                    itemId: 'suspect-flag',
                    dataIndex: 'allowSuspectData',
                    fieldLabel: Uni.I18n.translate('copyFromReference.suspectFlag', 'CFG', 'Allow suspect date'),
                    width: '100%'
                },
                {
                    xtype: 'checkbox',
                    itemId: 'complete',
                    dataIndex: 'completePeriod',
                    fieldLabel: Uni.I18n.translate('copyFromReference.complete', 'CFG', 'Complete period'),
                    width: '100%'
                },
                {
                    xtype: 'combobox',
                    itemId: 'estimation-comment',
                    fieldLabel: Uni.I18n.translate('copyFromReference.estimationComment', 'CFG', 'Estimation comment'),
                    width: '100%'
                },
                {
                    xtype: 'fieldcontainer',
                    fieldLabel: '&nbsp;',
                    margin: '20 0 0 0',
                    items: [
                        {
                            xtype: 'button',
                            itemId: 'copy-reading-button',
                            text: Uni.I18n.translate('general.copy', 'CFG', 'Copy'),
                            ui: 'action'
                        },
                        {
                            xtype: 'button',
                            itemId: 'cancel-button',
                            text: Uni.I18n.translate('general.cancel', 'CFG', 'Cancel'),
                            ui: 'link',
                            handler: function () {
                                me.close();
                            }
                        }
                    ]
                }
            ]
        };

        if (true) {
            me.items.items.splice(2, 1,
                {
                    xtype: 'combobox',
                    itemId: 'usage-point-field',
                    dataIndex: 'referenceUsagePoint',
                    fieldLabel: Uni.I18n.translate('copyFromReference.usagePoint', 'CFG', 'Check usage point'),
                    required: true,
                    width: '100%',
                    name: 'usagePointImpl',
                    store: 'Cfg.store.AllUsagePoint',
                    valueField: 'id',
                    displayField: 'name',
                    queryMode: 'remote',
                    remoteFilter: true,
                    queryParam: 'like',
                    queryCaching: false,
                    minChars: 1,
                    editable: true,
                    typeAhead: true,
                    emptyText: Uni.I18n.translate('copyFromReference.selectUsagePoint', 'CFG', 'Select usage point'),
                    // listeners: {
                    //     change: {
                    //         fn: function (implementationCombo, newValue) {
                    //             var estimator = implementationCombo.getStore().getById(newValue);
                    //
                    //             estimator && me.down('property-form').loadRecord(estimator);
                    //             me.updateLayout();
                    //             me.center();
                    //         }
                    //     }
                    // }
                },
                {
                    xtype: 'combobox',
                    itemId: 'purpose-field',
                    dataIndex: 'metrologyPurpose',
                    fieldLabel: Uni.I18n.translate('copyFromReference.purpose', 'CFG', 'Check purpose'),
                    required: true,
                    width: '100%',
                    name: 'purposeImpl',
                    store: 'Cfg.store.AllPurpose',
                    valueField: 'id',
                    displayField: 'name',
                    queryMode: 'remote',
                    remoteFilter: true,
                    queryParam: 'name',
                    queryCaching: false,
                    minChars: 1,
                    editable: true,
                    typeAhead: true,
                    emptyText: Uni.I18n.translate('copyFromReference.selectPurpose', 'CFG', 'Select purpose'),
                    // listeners: {
                    //     change: {
                    //         fn: function (implementationCombo, newValue) {
                    //             var estimator = implementationCombo.getStore().getById(newValue);
                    //
                    //             estimator && me.down('property-form').loadRecord(estimator);
                    //             me.updateLayout();
                    //             me.center();
                    //         }
                    //     }
                    // }
                }
            );
        }
        me.callParent(arguments);
    },

    // getRecord: function () {
    //     var me = this;
    //
    //     return me.getRecord();
    //
    // },

    getEstimator: function () {
        var me = this,
            record = me.down('#estimator-field').getStore().getById(me.down('#estimator-field').getValue());
        return record ? record.get('estimatorImpl') : '';
    }
});