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
                itemId: 'meter-meter',
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
                    forceSelection: true,
                    cls: 'stretchy-combo',
                    listeners: me.meterComboLiseners
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
                    // width: 120,
                    iconCls: 'icon-cancel-circle2 remove-button-grid',
                    tooltip: Uni.I18n.translate('general.UnlinkMeter', 'UNI', 'Unlink meter'),
                    handler: function (grid, rowIndex) {
                        var colNum = 1,
                            cellSelector = grid.getCellSelector(grid.up('grid').columns[colNum]),
                            domEl = grid.getEl().query(cellSelector)[colNum],
                            comboDom = Ext.get(domEl.getAttribute('id')).query('.stretchy-combo')[rowIndex],
                            combo = Ext.getCmp(comboDom.getAttribute('id'));
                        combo.clearValue()
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
        },

        change: function (field, newValue) {
            if (Ext.isEmpty(newValue)) {
                field.reset();
            }
        }
    }
});