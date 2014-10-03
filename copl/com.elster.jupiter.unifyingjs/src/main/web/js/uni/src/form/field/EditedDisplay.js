/**
 * @class Uni.form.field.EditedDisplay
 */
Ext.define('Uni.form.field.EditedDisplay', {
    extend: 'Ext.form.field.Display',
    xtype: 'edited-displayfield',
    name: 'editedDate',

    deferredRenderer: function (field, icon) {
        field.getEl().down('.x-form-display-field').appendChild(icon);
        field.updateLayout();
    },

    renderer: function (value, field) {
        var icon;

        if (value) {
            value = Ext.isDate(value) ? value : new Date(value);
            icon = document.createElement('span');
            icon.className = 'icon-edit';
            Ext.create('Ext.tip.ToolTip', {
                target: icon,
                html: Uni.I18n.formatDate('editedDate.format',value, 'MDC', '\\E\\d\\i\\t\\e\\d \\o\\n F d, Y \\a\\t H:i')
            });
            Ext.defer(this.deferredRenderer, 1, this, [field, icon]);
        }
        return '';
    }
});