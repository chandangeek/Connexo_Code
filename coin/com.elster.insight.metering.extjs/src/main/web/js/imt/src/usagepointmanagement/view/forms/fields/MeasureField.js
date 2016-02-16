Ext.define('Imt.usagepointmanagement.view.forms.fields.MeasureField', {
    extend: 'Ext.form.FieldContainer',
    mixins: {
        field: 'Ext.form.field.Field'
    },
    alias: 'widget.measurefield',
    layout: 'hbox',
    combineErrors: true,
    msgTarget: 'under',

    initComponent: function () {
        var me = this;

        me.items = [
            {
                xtype: 'numberfield',
                minValue: 0,
                autoStripChars: true,
                allowExponential: false,
                margin: '0 10 0 0',
                flex: 1
            },
            {
                xtype: 'combobox',
                store: me.store || 'ext-empty-store',
                width: 80,
                displayField: 'displayValue',
                valueField: 'id',
                queryMode: 'local',
                forceSelection: true
            }
        ];

        me.callParent(arguments);

        if (me.value) {
            me.setValue(me.value);
        }
    },

    setValue: function (value) {
        var me = this;

        me.value = value;
    },

    getValue: function () {
        var me = this;

        return me.value;
    },

    getRawValue: function () {
        var me = this;

        return me.getValue().toString();
    },

    markInvalid: function (error) {
        var me = this;

        me.toggleInvalid(error);
    },

    clearInvalid: function () {
        var me = this;

        me.toggleInvalid();
    },

    toggleInvalid: function (error) {
        var me = this,
            oldError = me.getActiveError();

        Ext.suspendLayouts();
        me.items.each(function (item) {
            if (item.isFormField) {
                if (error) {
                    item.addCls('x-form-invalid');
                } else {
                    item.removeCls('x-form-invalid');
                }
            }
        });
        if (error) {
            me.setActiveErrors(error);
        } else {
            me.unsetActiveError();
        }
        if (oldError !== me.getActiveError()) {
            me.doComponentLayout();
        }
        Ext.resumeLayouts(true);
    }
});