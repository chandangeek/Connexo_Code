/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.view.usagepointregister.CopyFromReferenceWindow', {
    extend: 'Ext.window.Window',
    alias: 'widget.reading-copy-from-reference-window',
    modal: true,
    title: Uni.I18n.translate('general.copyFromReference', 'CFG', 'Copy from reference'),
    records: null,
    usagePoint: null,
    frame: true,
    monitorResize: false,

    requires: [
        'Uni.util.FormErrorMessage',
        'Uni.property.form.Property',
        'Uni.property.view.DefaultButton',
        'Uni.property.store.EstimationComment'
    ],

    initComponent: function () {
        var me = this;

        me.items = {
            xtype: 'form',
            itemId: 'reading-copy-window-form',
            padding: 5,
            defaults: {
                width: 500,
                labelWidth: 170,
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
                    flex: 1,
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
                    flex: 1,
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
                            value: Array.isArray(me.records) ? me.records[0].get('interval').end : me.records.get('interval').end || new Date(),
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
                    flex: 1
                },
                {
                    xtype: 'fieldcontainer',
                    fieldLabel: Uni.I18n.translate('copyFromReference.complete', 'CFG', 'Complete period'),
                    flex: 1,
                    layout: 'hbox',
                    items: [
                        {
                            xtype: 'checkbox',
                            itemId: 'complete',
                            name: 'completePeriod'
                        },
                        {
                            xtype: 'label',
                            margin: 6,
                            text: Uni.I18n.translate('copyFromReference.completeBoxMassage', 'CFG', 'Only copy when all data is present')
                        }
                    ]
                },
                {
                    xtype: 'fieldcontainer',
                    fieldLabel: Uni.I18n.translate('copyFromReference.estimationComment', 'CFG', 'Estimation comment'),
                    flex: 1,
                    layout: 'hbox',
                    items: [
                        {
                            xtype: 'combobox',
                            itemId: 'estimation-comment',
                            flex: 1,
                            // name: 'estimationComment',
                            // store: 'Uni.property.store.EstimationComment',
                            // valueField: 'id',
                            // displayField: 'comment',
                            // queryMode: 'local',
                            minChars: 1,
                            editable: true,
                            typeAhead: true,
                            listeners: {
                                afterrender: function () {
                                    this.button = me.down('#estimation-comment-default-button');
                                },
                                change: function (combobox) {
                                    if (combobox.getValue()) {
                                        this.button.setDisabled(false);
                                    } else {
                                        this.button.setDisabled(true);
                                    }
                                }
                            }
                        },
                        {
                            xtype: 'uni-default-button',
                            itemId: 'estimation-comment-default-button',
                            hidden: false,
                            disabled: true,
                            handler: function () {
                                me.down('#estimation-comment').reset();
                            }

                        }
                    ]

                },
                {
                    xtype: 'fieldcontainer',
                    fieldLabel: Uni.I18n.translate('copyFromReference.projectedValue', 'CFG', 'Projected value'),
                    flex: 1,
                    layout: 'hbox',
                    items: [
                        {
                            xtype: 'checkbox',
                            itemId: 'projected',
                            name: 'projecterdValue'
                        },
                        {
                            xtype: 'label',
                            margin: 6,
                            text: Uni.I18n.translate('copyFromReference.projectedBoxMassage', 'CFG', 'Mark values as projected')
                        }
                    ]
                },
                {
                    xtype: 'fieldcontainer',
                    fieldLabel: '&nbsp;',
                    margin: '10 10 0 0',
                    style: {
                        display: 'inline-block'
                    },
                    items: [
                        {
                            xtype: 'button',
                            itemId: 'copy-reading-button',
                            margin: '0 0 0 40',
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
                    flex: 1,
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
                    name: 'referencePurpose',
                    fieldLabel: Uni.I18n.translate('copyFromReference.purpose', 'CFG', 'Check purpose'),
                    required: true,
                    flex: 1,
                    store: 'Cfg.store.AllPurpose',
                    valueField: 'id',
                    displayField: 'name',
                    queryCaching: false,
                    minChars: 1,
                    editable: true,
                    typeAhead: true,
                    emptyText: Uni.I18n.translate('copyFromReference.selectPurpose', 'CFG', 'Select purpose')
                }
            );
        }
        me.callParent(arguments);
    }
});