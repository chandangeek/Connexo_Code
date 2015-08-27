Ext.define('Mdc.view.setup.device.form.DeviceDateField', {
    extend: 'Ext.form.field.Display',
    xtype: 'deviceFormDateField',
    fullInfo: false,

    initComponent: function () {
        var me = this;

        me.renderer = function (value) {
            if (value && (value.available || me.fullInfo)) {
                me.show();
                if (Ext.isEmpty(value.displayValue)) {
                    return '-'
                } else {
                    return Uni.DateTime.formatDateTimeShort(new Date(value.displayValue));
                }
            } else {
                me.hide();
                return null;
            }

            me.callParent(arguments);
        }
    }
});



