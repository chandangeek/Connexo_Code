/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.view.usagepointregister.CopyFromReferenceWindow', {
    extend: 'Ext.window.Window',
    alias: 'widget.reading-copy-from-reference-window',
    modal: true,
    hideCollapseTool: true,
    title: Uni.I18n.translate('general.copyFromReference', 'CFG', 'Copy from reference'),
    records: null,
    usagePoint: null,

    requires: [
        'Uni.util.FormErrorMessage',
        'Uni.property.form.Property'
    ],

    initComponent: function () {
        var me = this;

        me.items = {
            xtype: 'form',
            itemId: 'reading-copy-window-form',
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
                    name: 'referenceDevice',
                    fieldLabel: Uni.I18n.translate('copyFromReference.device', 'CFG', 'Check device'),
                    required: true,
                    width: '100%',
                    store: 'Cfg.store.AllDevices',
                    valueField: 'name',
                    displayField: 'name',
                    queryMode: 'remote',
                    remoteFilter: true,
                    queryParam: 'name',
                    queryCaching: false,
                    minChars: 1,
                    editable: true,
                    typeAhead: true,
                    emptyText: Uni.I18n.translate('copyFromReference.selectDevice', 'CFG', 'Select device')
                },
                {
                    xtype: 'combobox',
                    itemId: 'readingType-field',
                    name: 'readingType',
                    fieldLabel: Uni.I18n.translate('copyFromReference.readingType', 'CFG', 'Check reading type'),
                    required: true,
                    width: '100%',
                    store: 'Cfg.store.AllReadingTypes',
                    valueField: 'mRID',
                    displayField: 'fullAliasName',
                    queryMode: 'remote',
                    remoteFilter: true,
                    queryParam: 'value',
                    queryCaching: false,
                    minChars: 1,
                    editable: true,
                    typeAhead: true,
                    emptyText: Uni.I18n.translate('copyFromReference.selectReadingType', 'CFG', 'Select reading type')
                },
                {
                    xtype: 'fieldcontainer',
                    itemId: 'start-date-container',
                    fieldLabel: Uni.I18n.translate('copyFromReference.startDate', 'CFG', 'Start date/time'),
                    defaults: {
                        width: '100%'
                    },
                    items: [
                        {
                            xtype: 'date-time',
                            itemId: 'start-date-field',
                            name: 'startDate',
                            value: new Date(),
                            layout: 'hbox',
                            valueInMilliseconds: true
                        }
                    ]
                },
                {
                    xtype: 'checkbox',
                    itemId: 'suspect-flag',
                    name: 'allowSuspectData',
                    fieldLabel: Uni.I18n.translate('copyFromReference.suspectFlag', 'CFG', 'Allow suspect date'),
                    width: '100%'
                },
                {
                    xtype: 'checkbox',
                    itemId: 'complete',
                    name: 'completePeriod',
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

        if (me.usagePoint) {
            me.items.items.splice(2, 1,
                {
                    xtype: 'combobox',
                    itemId: 'usage-point-field',
                    name: 'referenceUsagePoint',
                    fieldLabel: Uni.I18n.translate('copyFromReference.usagePoint', 'CFG', 'Check usage point'),
                    required: true,
                    width: '100%',
                    store: 'Cfg.store.AllUsagePoint',
                    valueField: 'name',
                    displayField: 'name',
                    queryMode: 'remote',
                    remoteFilter: true,
                    queryParam: 'like',
                    queryCaching: false,
                    minChars: 1,
                    editable: true,
                    typeAhead: true,
                    emptyText: Uni.I18n.translate('copyFromReference.selectUsagePoint', 'CFG', 'Select usage point')
                },
                {
                    xtype: 'combobox',
                    itemId: 'purpose-field',
                    name: 'metrologyPurpose',
                    fieldLabel: Uni.I18n.translate('copyFromReference.purpose', 'CFG', 'Check purpose'),
                    required: true,
                    width: '100%',
                    store: 'Cfg.store.AllPurpose',
                    valueField: 'id',
                    displayField: 'name',
                    queryCaching: false,
                    minChars: 1,
                    editable: true,
                    typeAhead: true,
                    emptyText: Uni.I18n.translate('copyFromReference.selectPurpose', 'CFG', 'Select purpose'),
                }
            );
        }
        me.callParent(arguments);
    }
});