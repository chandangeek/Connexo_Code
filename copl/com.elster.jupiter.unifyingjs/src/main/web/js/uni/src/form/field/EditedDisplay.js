/**
 * @class Uni.form.field.EditedDisplay
 */
Ext.define('Uni.form.field.EditedDisplay', {
    extend: 'Ext.form.field.Display',
    xtype: 'edited-displayfield',
    name: 'editedDate',
    emptyText: '',

    renderer: function (value) {
        var result,
            date,
            iconClass,
            tooltipText;

        if (value) {
            date = Ext.isDate(value.date) ? value.date : new Date(value.date);
            switch (value.flag) {
                case 'ADDED':
                    iconClass = 'icon-edit';
                    tooltipText = Uni.I18n.formatDate('addedDate.format', date, 'MDC', '\\A\\d\\d\\e\\d \\o\\n F d, Y \\a\\t H:i');
                    break;
                case 'EDITED':
                    iconClass = 'icon-edit';
                    tooltipText = Uni.I18n.formatDate('editedDate.format', date, 'MDC', '\\E\\d\\i\\t\\e\\d \\o\\n F d, Y \\a\\t H:i');
                    break;
                case 'REMOVED':
                    iconClass = 'icon-remove';
                    tooltipText = Uni.I18n.formatDate('removedDate.format', date, 'MDC', '\\R\\e\\m\\o\\v\\e\\d \\o\\n F d, Y \\a\\t H:i');
                    break;
            }
            if (iconClass && tooltipText) {
                result = '<span class="' + iconClass + '" data-qtip="' + tooltipText + '"></span>';
            }
        }

        return result || this.emptyText;
    }
});