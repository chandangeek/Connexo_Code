Ext.define('Imt.usagepointmanagement.view.forms.fields.meteractivations.MeterActivationsGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.meter-activations-grid',
    requires: [
        'Uni.form.field.DateTime',
        'Uni.grid.plugin.EditableCells',
        'Uni.grid.plugin.ShowConditionalToolTip'
    ],
    store: Ext.create('Ext.data.Store', {
        fields: ['meterRole', 'meter', 'activationTime']
    }),
    disableSelection: true,
    meterRoles: null,
    plugins: [
        {
            ptype: 'editableCells'
        },
        {
            ptype: 'showConditionalToolTip'
        }
    ],

    initComponent: function () {
        var me = this;

        me.columns = [
            {
                header: Uni.I18n.translate('general.meterRole', 'IMT', 'Meter role'),
                dataIndex: 'meterRole',
                flex: 0.5,
                renderer: function (value, metaData) {
                    metaData.tdCls = Ext.baseCSSPrefix + 'td-content-middle';
                    return value ? value.name : '-';
                }
            },
            {
                header: Uni.I18n.translate('general.meter', 'IMT', 'Meter'),
                dataIndex: 'meter',
                disableTooltip: true,
                flex: 1,
                editor: {
                    xtype: 'combo',
                    width: 270,
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
                        afterrender: function(field){
                            if (me.meterRoles && !field.value) {
                                document.getElementsByClassName('unlink-miter-grid')[0].style.display = 'none';
                            } else {
                                document.getElementsByClassName('unlink-miter-grid')[0].style.display = 'inline-block';
                            }
                        },
                        change: function (field, newValue) {
                            // debugger;
                            if (me.meterRoles && !newValue) {
                                document.getElementsByClassName('unlink-miter-grid')[0].style.display = 'none';
                            } else {
                                document.getElementsByClassName('unlink-miter-grid')[0].style.display = 'inline-block';
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
                    width: 400,
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
                    }
                }
            }
        ];
        if (me.meterRoles) {
            me.columns.push(
                {
                    xtype: 'uni-actioncolumn-remove',
                    iconCls: 'icon-cancel-circle2 unlink-miter-grid',
                    tooltip: Uni.I18n.translate('general.UnlinkMeter', 'IMT', 'Unlink meter'),
                    handler: function (grid, rowIndex) {
                        var cellSelector = grid.getCellSelector(grid.up('grid').columns[1]),
                            domEl = grid.getEl().query(cellSelector)[++rowIndex],
                            comboDom = Ext.get(domEl.getAttribute('id')).query('.stretchy-combo')[0],
                            combo = Ext.getCmp(comboDom.getAttribute('id'));
                        combo.clearValue();
                    }
                }
            );
        }
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