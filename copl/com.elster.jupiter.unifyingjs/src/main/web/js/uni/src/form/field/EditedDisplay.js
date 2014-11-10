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
        var icon,
            date,
            tooltipText;

        if (value) {
            date = Ext.isDate(value.date) ? value.date : new Date(value.date);
            icon = document.createElement('span');
            switch (value.flag) {
                case 'ADDED':
                    icon.className = 'icon-edit';
                    tooltipText = Uni.I18n.formatDate('addedDate.format', date, 'MDC', '\\A\\d\\d\\e\\d \\o\\n F d, Y \\a\\t H:i');
                    break;
                case 'EDITED':
                    icon.className = 'icon-edit';
                    tooltipText = Uni.I18n.formatDate('editedDate.format', date, 'MDC', '\\E\\d\\i\\t\\e\\d \\o\\n F d, Y \\a\\t H:i');
                    break;
                case 'REMOVED':
                    icon.className = 'icon-remove';
                    tooltipText = Uni.I18n.formatDate('removedDate.format', date, 'MDC', '\\R\\e\\m\\o\\v\\e\\d \\o\\n F d, Y \\a\\t H:i');
                    break;
            }
            Ext.create('Ext.tip.ToolTip', {
                target: icon,
                html: tooltipText
            });
            Ext.defer(this.deferredRenderer, 1, this, [field, icon]);
        }
        return '';
    }
});