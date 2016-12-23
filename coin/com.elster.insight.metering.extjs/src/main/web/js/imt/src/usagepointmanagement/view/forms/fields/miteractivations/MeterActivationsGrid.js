Ext.define('Imt.usagepointmanagement.view.forms.fields.miteractivations.MeterActivationsGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.meter-activations-grid',
    requires: [
        'Uni.form.field.ComboReturnedRecordData',
        'Uni.form.field.DateTime',
        'Uni.grid.plugin.EditableCells',
        'Uni.grid.plugin.ShowConditionalToolTip'
    ],
    store: Ext.create('Ext.data.Store', {
        fields: ['meterRole', 'meter', 'activationDate']
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
                disableTooltip: true,
                flex: 1,
                editor: {
                    xtype: 'combo-returned-record-data',
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
                    forceSelection: false,
                    cls: 'stretchy-combo',
                    listeners: me.meterComboLiseners
                }
            },
            {
                header: Uni.I18n.translate('general.activationDate', 'IMT', 'Activation date'),
                dataIndex: 'activationDate',
                disableTooltip: true,
                width: 310,
                editor: {
                    xtype: 'date-time',
                    itemId: 'installation-time-date',
                    layout: 'hbox',
                    width: '100%',
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