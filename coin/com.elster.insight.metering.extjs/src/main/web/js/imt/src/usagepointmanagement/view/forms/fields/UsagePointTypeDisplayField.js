Ext.define('Imt.usagepointmanagement.view.forms.fields.UsagePointTypeDisplayField', {
    extend: 'Ext.form.field.Display',
    alias: 'widget.usagepointtypedisplayfield',

    renderer: function (value) {
        var result = '-',
            type;

        if (!Ext.isEmpty(value)) {
            result = Ext.getStore('Imt.usagepointmanagement.store.UsagePointTypes').getById(value).get('displayName');
        }

        return result;
    }
});