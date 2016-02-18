Ext.define('Mdc.view.setup.comtasks.ComtaskActionPreviewForm', {
    extend: 'Ext.form.Panel',
    xtype: 'comtaskActionPreviewForm',
    border: false,

    layout: {
        type: 'vbox',
        align: 'stretch'
    },

    defaults: {
        xtype: 'displayfield',
        labelWidth: 350,
        width: 400
    },

    items: [
    ],

    addParameter: function(labelText, value) {
        var me = this,
            field = Ext.create('Ext.form.field.Display', Ext.apply(me.defaults, {
                fieldLabel: labelText,
                value: value,
                renderer: function(value) {
                    return value.replace(/\n/g, '<br>');
                }
            }));

        me.add(field);
    },

    reinitialize: function() {
        this.removeAll();
    }
});