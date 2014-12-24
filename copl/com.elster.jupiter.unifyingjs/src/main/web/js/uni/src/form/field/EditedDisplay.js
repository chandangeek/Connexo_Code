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
                    tooltipText = Uni.I18n.translate('general.addedOn', 'UNI', 'Added on') + ' '
                        + Uni.DateTime.formatDateLong(date)
                        + ' ' + Uni.I18n.translate('general.at', 'UNI', 'At').toLowerCase() + ' '
                        + Uni.DateTime.formatTimeLong(date);
                    break;
                case 'EDITED':
                    iconClass = 'icon-edit';
                    tooltipText = Uni.I18n.translate('general.editedOn', 'UNI', 'Edited on') + ' '
                        + Uni.DateTime.formatDateLong(date)
                        + ' ' + Uni.I18n.translate('general.at', 'UNI', 'At').toLowerCase() + ' '
                        + Uni.DateTime.formatTimeLong(date);
                    break;
                case 'REMOVED':
                    iconClass = 'icon-remove';
                    tooltipText = Uni.I18n.translate('general.removedOn', 'UNI', 'Removed on') + ' '
                        + Uni.DateTime.formatDateLong(date)
                        + ' ' + Uni.I18n.translate('general.at', 'UNI', 'At').toLowerCase() + ' '
                        + Uni.DateTime.formatTimeLong(date);
                    break;
            }
            if (iconClass && tooltipText) {
                result = '<span class="' + iconClass + '" data-qtip="' + tooltipText + '"></span>';
            }
        }

        return result || this.emptyText;
    }
});