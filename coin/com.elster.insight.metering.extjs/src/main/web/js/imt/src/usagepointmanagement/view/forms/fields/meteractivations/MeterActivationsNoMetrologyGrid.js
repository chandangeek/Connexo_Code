/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointmanagement.view.forms.fields.meteractivations.MeterActivationsNoMetrologyGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.meter-activations-no-metrology-grid',
    requires: [
        'Uni.form.field.DateTime',
        'Uni.grid.plugin.EditableCells',
        'Uni.grid.plugin.ShowConditionalToolTip',
        'Imt.usagepointmanagement.store.AllMeterRoles'
    ],
    store: Ext.create('Ext.data.Store', {
        fields: ['meterRole', 'meter', 'activationTime', 'isAddRow'],
        getTotalCount: function () {
            return this.getCount() - 1;
        }
    }),
    disableSelection: true,
    plugins: [
        {
            ptype: 'editableCells'
        },
        {
            ptype: 'showConditionalToolTip'
        }
    ],
    usagePoint: null,
    initComponent: function () {
        var me = this;

        me.columns = [
            {
                header: Uni.I18n.translate('general.meterRole', 'IMT', 'Meter role'),
                dataIndex: 'meterRole',
                flex: 0.8,
                editor: {
                    xtype: 'combo',
                    width: 220,
                    multiSelect: false,
                    emptyText: Uni.I18n.translate('usagepoint.meterRole.select', 'IMT', 'Select a meter role'),
                    store: new Ext.create('Imt.usagepointmanagement.store.AllMeterRoles'),
                    displayField: 'displayName',
                    valueField: 'key',
                    cls: 'stretchy-combo',
                    listeners: {
                        afterrender: function (field) {
                            field.bindStore(new Ext.create('Imt.usagepointmanagement.store.AllMeterRoles'));
                            if (field.cell.record.get('isAddRow') == true) {
                                field.setVisible(false);
                            }
                        },
                        expand: function (field) {
                            var store = field.getStore();

                            store.filters.clear();
                            store.filter({
                                filterFn: function (item) {
                                    return field.cell.record.get('meterRole') == item.get('key') ||
                                        me.getStore().find('meterRole', item.get('key')) == -1;
                                }
                            });
                        }
                    },
                    setValue: function (value) {
                        if (value && Object.prototype.toString.call(value) == "[object Array]") {
                            Ext.getClass(this).prototype.setValue.apply(this, [value[0].get('key')]);
                        }
                        else if (value && Object.prototype.toString.call(value) == "[object Object]") {
                            Ext.getClass(this).prototype.setValue.apply(this, [value.id]);
                        }
                        else if (value && typeof value == 'string') {
                            Ext.getClass(this).prototype.setValue.apply(this, [value]);
                        }
                        return this;
                    }
                }
            },
            {
                header: Uni.I18n.translate('general.meter', 'IMT', 'Meter'),
                dataIndex: 'meter',
                itemId: 'cell-meter',
                disableTooltip: true,
                flex: 0.8,
                editor: {
                    xtype: 'combo',
                    width: 220,
                    fieldType: 'meterCombo',
                    multiSelect: false,
                    emptyText: Uni.I18n.translate('usagepoint.setMeters.strtTyping', 'IMT', 'Start typing to select a meter'),
                    store: 'Imt.usagepointsetup.store.Devices',
                    displayField: 'name',
                    valueField: 'name',
                    anyMatch: true,
                    queryMode: 'remote',
                    queryParam: 'like',
                    queryCaching: false,
                    minChars: 1,
                    loadStore: false,
                    cls: 'stretchy-combo',
                    listeners: Ext.merge(me.meterComboLiseners, {
                        afterrender: function (field) {
                            if (field.cell.record.get('isAddRow') == true) {
                                var index = me.getView().getNodeByRecord(field.cell.record).getAttribute('data-recordindex'),
                                    className = document.getElementsByClassName('unlink-miter-grid')[index].className;

                                document.getElementsByClassName('unlink-miter-grid')[index].attributes['data-qtip'].value = Uni.I18n.translate('general.linkMeter', 'IMT', 'Link meter');
                                document.getElementsByClassName('unlink-miter-grid')[index].className = className.replace(/icon-cancel-circle2/g, 'icon-plus-circle2');
                                field.setVisible(false);
                            }
                        }
                    })
                }
            },
            {
                header: Uni.I18n.translate('general.activationDate', 'IMT', 'Activation date'),
                dataIndex: 'activationTime',
                disableTooltip: true,
                flex: 1,
                editor: {
                    xtype: 'date-time',
                    itemId: 'installation-time-date',
                    valueInMilliseconds: true,
                    layout: 'hbox',
                    width: 420,
                    dateConfig: {
                        width: 110
                    },
                    dateTimeSeparatorConfig: {
                        html: Uni.I18n.translate('general.at', 'IMT', 'At').toLowerCase(),
                        style: 'color: #686868'
                    },
                    hoursConfig: {
                        width: 60
                    },
                    minutesConfig: {
                        width: 60
                    },
                    listeners: {
                        afterrender: function (field) {
                            if (field.cell.record.get('isAddRow') == true) {
                                field.setVisible(false);
                            }
                        }
                    }
                }
            },
            {
                xtype: 'uni-actioncolumn-remove',
                iconCls: 'icon-cancel-circle2 unlink-miter-grid',
                tooltip: Uni.I18n.translate('general.UnlinkMeter', 'IMT', 'Unlink meter'),
                handler: function (grid, rowIndex) {
                    if (grid.getStore().getAt(rowIndex).get('isAddRow') == true) {
                        me.getStore().insert(me.getStore().count() - 1, {activationTime: me.usagePoint.get('createTime')});
                        me.reconfigure();
                    }
                    else {
                        grid.getStore().remove([grid.getStore().getAt(rowIndex)]);
                        me.reconfigure();
                    }
                    me.down('pagingtoolbartop').updateInfo();
                }
            }
        ];

        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('metrologyConfigurationDetails.meterRolesCount', 'IMT', '{2} meter role(s)'),
                isFullTotalCount: true,
                noBottomPaging: true,
                exportButton: false
            }
        ];
        me.callParent(arguments);
    },

    meterComboLiseners: {
        expand: function (combo) {
            var picker = combo.getPicker(),
                fn = function (view) {
                    var store = view.getStore(),
                        el = view.getEl().down('.' + Ext.baseCSSPrefix + 'list-plain');

                    if (store.getTotalCount() > store.getCount()) {
                        el.appendChild({
                            tag: 'li',
                            html: Uni.I18n.translate('usagePoint.setMeters.keepTyping', 'IMT', 'Keep typing to narrow down'),
                            cls: Ext.baseCSSPrefix + 'boundlist-item combo-limit-notification'
                        });
                    }
                };

            picker.on('refresh', fn);
            picker.on('beforehide', function () {
                picker.un('refresh', fn);
            }, combo, {single: true});
        }
    }
});