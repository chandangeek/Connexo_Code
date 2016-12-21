Ext.define('Imt.usagepointmanagement.view.forms.fields.MeterActivationsField', {
    extend: 'Uni.view.container.EmptyGridContainer',
    requires: [
        'Uni.util.FormInfoMessage'
    ],
    alias: 'widget.meter-activations-field',

    mixins: {
        field: 'Ext.form.field.Field',
        bindable: 'Ext.util.Bindable'
    },

    initComponent: function () {
        var me = this;

        me.grid = {
            xtype: 'grid',
            itemId: 'meter-activations-grid',
            title: Uni.I18n.translate('general.meterRoles', 'IMT', 'Meter roles'),
            ui: 'medium',
            style: 'padding-left: 0;padding-right: 0;',
            store: Ext.create('Ext.data.Store', {
                fields: ['meterRole', 'meter', 'activationDate']
            }),
            plugins: {
                ptype: 'cellediting',
                clicksToEdit: 1
            },
            columns: [
                {
                    header: Uni.I18n.translate('general.meterRole', 'IMT', 'Meter role'),
                    dataIndex: 'meterRole',
                    flex: 1,
                    renderer: function (value) {
                        return value ? value.name : '-';
                    }
                },
                {
                    header: Uni.I18n.translate('general.meter', 'IMT', 'Meter'),
                    dataIndex: 'meter',
                    flex: 1,
                    editor: {
                        xtype: 'combobox',
                        labelWidth: 120,
                        width: 360,
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
                        listeners: me.meterComboLiseners
                    }
                },
                {
                    header: Uni.I18n.translate('general.activationDate', 'IMT', 'Activation date'),
                    dataIndex: 'activationDate',
                    flex: 1,
                    renderer: function (value) {
                        return value ? Uni.DateTime.formatDateTimeShort(value) : '-';
                    },
                    editor: {
                        xtype: 'date-time',
                        itemId: 'installation-time-date',
                        required: true,
                        layout: 'hbox',
                        dateConfig: {
                            width: 128
                        },
                        dateTimeSeparatorConfig: {
                            html: Uni.I18n.translate('general.at', 'IMT', 'At').toLowerCase(),
                            style: 'color: #686868'
                        },
                        hoursConfig: {
                            width: 80
                        },
                        minutesConfig: {
                            width: 80
                        }
                    }
                }
            ]
        };

        me.emptyComponent = {
            xtype: 'uni-form-info-message',
            text: Uni.I18n.translate('meterActivationsField.emptyMessage', 'IMT', 'No meter roles defined on the selected metrology configuration')
        };

        me.callParent(arguments);
    },

    setMeterRoles: function (meterRoles, usagePointCreationDate) {
        var me = this,
            store = me.down('#meter-activations-grid').getStore(),
            data = _.map(meterRoles,
                function (meterRole) {
                    return {
                        meterRole: meterRole,
                        activationDate: usagePointCreationDate ? new Date(usagePointCreationDate) : new Date()
                    }
                }
            );

        store.loadData(data);
        store.fireEvent('load', data);
    },

    getValue: function () {
        var me = this,
            store = me.down('#meter-activations-grid').getStore(),
            value = [];

        store.each(function (record) {
            var result = record.getData();

            if (!Ext.isEmpty(result.meter)) {
                result.activationDate = Ext.isDate(result.activationDate) ? result.activationDate.getTime() : null;
                result.meter = {name: result.meter};

                value.push(result);
            }
        });

        return !Ext.isEmpty(value) ? value : null;
    },

    getRawValue: function () {
        var me = this;

        return me.getValue().toString();
    },

    setValue: Ext.emptyFn,

    markInvalid: Ext.emptyFn,

    clearInvalid: Ext.emptyFn,

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
        change: function (combo, newValue) {
            var index = combo.getStore().findExact('name', newValue);
            if (index >= 0) {
                combo.meterData = combo.getStore().getAt(index).getData();
            } else {
                combo.meterData = null
            }
        },
        blur: function (combo) {
            Ext.isEmpty(combo.meterData) && combo.setValue('');
        }
    }
});