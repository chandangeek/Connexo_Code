Ext.define('Isu.view.component.AssigneeControl', {
    extend: 'Ext.form.FieldContainer',
    alias: 'widget.issues-assignee-control',
    mixins: {
        field: 'Ext.form.field.Field'
    },
    defaults: {
        xtype: 'container',
        layout: {
            type: 'hbox'
        },
        margin: '0 0 8 0'
    },

    getValue: function () {
        return this.down('radiofield[checked=true]').inputValue;
    },

    markInvalid: function (errors) {
        this.down('radiofield[checked=true]').nextSibling().markInvalid(errors);
    }
});