Ext.define('Imt.usagepointmanagement.view.forms.fields.meteractivations.MeterActivationsField', {
    extend: 'Uni.view.container.EmptyGridContainer',
    requires: [
        'Uni.util.FormInfoMessage',
        'Imt.usagepointmanagement.view.forms.fields.meteractivations.MeterActivationsGrid'
    ],
    alias: 'widget.meter-activations-field',

    mixins: {
        field: 'Ext.form.field.Field',
        bindable: 'Ext.util.Bindable'
    },
    meterRoles: null,
    initComponent: function () {
        var me = this;

        me.grid = {
            xtype: me.hasMetrologyConfiguration() ? 'meter-activations-grid' : 'meter-activations-no-metrology-grid',
            itemId: 'meter-activations-grid',
            meterRoles: me.meterRoles,
            usagePoint: me.usagePoint,
            maxHeight: 500,
            listeners: {
                edit: Ext.bind(me.onMeterActivationEdit, me)
            },
            bbar: {
                xtype: 'component',
                itemId: 'meter-activations-field-errors',
                cls: 'x-form-invalid-under',
                style: {
                    'white-space': 'normal'
                },
                hidden: true
            }
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
                        meter: meterRole.meter,
                        activationTime: usagePointCreationDate ? usagePointCreationDate : meterRole.activationTime ? meterRole.activationTime : new Date().getTime()
                    }
                }
            );

        if (me.hasMetrologyConfiguration() == false) {
            data.push({isAddRow: true});
        }
        store.loadData(data);
        store.fireEvent('load', data);
    },

    getValue: function () {
        var me = this,
            store = me.down('#meter-activations-grid').getStore(),
            value = [];

        store.each(function (record) {
            var result = record.getData();

            value.push(result);
        });

        return !Ext.isEmpty(value) ? value : null;
    },

    getRawValue: function () {
        var me = this;

        return me.getValue().toString();
    },

    setValue: Ext.emptyFn,

    markInvalid: function (errors) {
        var me = this,
            errorsField = me.down('#meter-activations-field-errors');

        Ext.suspendLayouts();
        errorsField.show();
        errorsField.update(errors.join('<br>'));
        Ext.resumeLayouts(true);
    },

    clearInvalid: function (errors) {
        var me = this,
            errorsField = me.down('#meter-activations-field-errors');

        Ext.suspendLayouts();
        errorsField.hide();
        errorsField.update();
        Ext.resumeLayouts(true);
    },

    onMeterActivationEdit: function (event) {
        var me = this,
            storeData = me.down('#meter-activations-grid').getStore().getRange(),
            allMetersSpecified = true;

        if (event.field.fieldType === 'meterCombo') {
            if (event.field.findRecordByValue(event.field.value)) {
                for (var i = 0; i < storeData.length; i++) {
                    if (Ext.isEmpty(storeData[i].get('meter'))) {
                        allMetersSpecified = false;
                        break;
                    }
                }
            } else {
                allMetersSpecified = false;
            }

            me.fireEvent('meterActivationsChange', allMetersSpecified);
        }
    },

    hasMetrologyConfiguration: function () {
        return this.usagePoint.get('metrologyConfiguration') != null;
    }
});