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
                flex: 1,
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
                    width: 300,
                    fieldType: 'meterCombo',
                    itemId: 'meter-combo',
                    // id: 'meter-combo',
                    class: 'meter-combo',
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
                    listeners: Ext.merge({
                        afterrender: function (combo) {
                            me.down('#clear-meters').combo = this;
                        }
                    },me.meterComboLiseners)
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
                    xtype: 'uni-actioncolumn',
                    width: 120,
                    itemId: 'clear-meters',
                    combo: null,
                    handler: function (grid, rowIndex, colIndex, item, e, record) {
                        // console.log(grid.getSelectionModel());
                        console.log(me.query('#meter-meter'));
                        console.log(me.query('#combobox'));
                        // console.log(Ext.query());

                        // var element = Ext.getCmp(elementId);
                        // console.log(element);
                        // console.log(grid.getGridColumns());
                        // console.log(grid.getStore().getAt(rowIndex).set('meter', ''));
                        // console.log(grid.getStore().load());
                        // console.log(grid.getVisibleColumnManager());
                        // console.log(grid.renderRow(record));
                        // console.log(me.down('#meter-combo-0'));
                        // me.meterComboLiseners.change(this.combo, null);
                        // this.combo.reset();
                    }
                }
            );
        }
        // me.plugins= [
        //     Ext.create('Ext.grid.plugin.RowEditing', {
        //         autoCancel: false
        //     })
        // ];

        me.callParent(arguments);
    },

    setMeterRoles: function (meterRoles, usagePointCreationDate) {
        var me = this,
            store = me.getStore(),
            data = _.map(meterRoles,
                function (meterRole) {
                    return {
                        meterRole: meterRole,
                        // activationTime: usagePointCreationDate ? usagePointCreationDate : new Date().getTime()
                    }
                }
            );

        store.loadData(data);
        store.fireEvent('load', data);
    },
    meterComboLiseners: {
        staterestore: function () {
            console.log('ssss');
        },
        expand: function (combo) {
            console.log(combo);
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
            console.log(field);
            if (Ext.isEmpty(newValue)) {
                field.reset();
            }
        }
    }
});