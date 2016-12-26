Ext.define('Imt.usagepointmanagement.view.forms.fields.miteractivations.MeterActivationsField', {
    extend: 'Uni.view.container.EmptyGridContainer',
    requires: [
        'Uni.util.FormInfoMessage',
        'Imt.usagepointmanagement.view.forms.fields.miteractivations.MeterActivationsGrid'
    ],
    alias: 'widget.meter-activations-field',

    mixins: {
        field: 'Ext.form.field.Field',
        bindable: 'Ext.util.Bindable'
    },

    initComponent: function () {
        var me = this;

        me.grid = {
            xtype: 'meter-activations-grid',
            itemId: 'meter-activations-grid',
            maxHeight: 500,
            listeners: {
                edit: Ext.bind(me.onMeterActivationEdit, me)
            },
            bbar: {
                xtype: 'component',
                itemId: 'meter-activations-field-errors',
                cls: 'x-form-invalid-under',
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
                        activationTime: usagePointCreationDate ? usagePointCreationDate : new Date().getTime()
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

    markInvalid: function (errors) {
        var me = this,
            errorsField = me.down('#meter-activations-field-errors');

        Ext.suspendLayouts();
        errorsField.show();
        errorsField.update(errors.split('<br>'));
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

        if (event.field.fieldType === 'meteCombo') {
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
    }
});