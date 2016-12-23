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
                //result.activationDate = Ext.isDate(result.activationDate) ? result.activationDate.getTime() : null;
                //result.meter = _.pick(result.meter, 'name');

                value.push(Ext.merge(result.meterRole, {
                    activationTime: Ext.isDate(result.activationDate) ? result.activationDate.getTime() : null,
                    meter: result.meter.name
                }));
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

    onMeterActivationEdit: function () {
        var me = this,
            storeData = me.down('#meter-activations-grid').getStore().getRange(),
            allMetersSpecified = true;

        for (var i = 0; i < storeData.length; i++) {
            if (Ext.isEmpty(storeData[i].get('meter'))) {
                allMetersSpecified = false;
            }
        }

        me.fireEvent('meterActivationsChange', allMetersSpecified);
    }
});