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
            formattedDate,
            iconClass,
            tooltipText,
            app = value && value.app ? value.app.name : undefined;

        if (value) {
            date = Ext.isDate(value.date) ? value.date : new Date(value.date);
            formattedDate = Uni.I18n.translate('general.dateAtTime', 'UNI', '{0} at {1}',
                [Uni.DateTime.formatDateLong(date), Uni.DateTime.formatTimeLong(date)]
            );
            switch (value.flag) {
                case 'ADDED':
                    iconClass = 'icon-pencil4';
                    tooltipText = app
                        ? Uni.I18n.translate('general.addedOnXApp', 'UNI', 'Added in {0} on {1}', [app, formattedDate])
                        : Uni.I18n.translate('general.addedOnX', 'UNI', 'Added on {0}', formattedDate);
                    break;
                case 'EDITED':
                    iconClass = 'icon-pencil4';
                    tooltipText = app
                        ? Uni.I18n.translate('general.editedOnXApp', 'UNI', 'Edited in {0} on {1}', [app, formattedDate])
                        : Uni.I18n.translate('general.editedOnX', 'UNI', 'Edited on {0}', formattedDate);
                    break;
                case 'ESTIMATED':
                    iconClass = 'icon-pencil4';
                    tooltipText = app
                        ? Uni.I18n.translate('general.estimatedOnXApp', 'UNI', 'Estimated in {0} on {1}', [app, formattedDate])
                        : Uni.I18n.translate('general.estimatedOnX', 'UNI', 'Estimated on {0}', formattedDate);
                    break;
                case 'REMOVED':
                    iconClass = 'icon-cancel-circle';
                    tooltipText = app
                        ? Uni.I18n.translate('general.removedOnXApp', 'UNI', 'Removed in {0} on {1}', [app, formattedDate])
                        : Uni.I18n.translate('general.removedOnX', 'UNI', 'Removed on {0}', formattedDate);
                    break;
                case 'RESET':
                    iconClass = 'icon-cancel-circle';
                    tooltipText = app
                        ? Uni.I18n.translate('general.resetOnXApp', 'UNI', 'Reset in {0} on {1}', [app, formattedDate])
                        : Uni.I18n.translate('general.resetOnX', 'UNI', 'Reset on {0}', formattedDate);
                    break;
            }
            if (iconClass && tooltipText) {
                result = '<span style="display: none;">' + value.flag + '</span><span class="' + iconClass + '" data-qtip="' + tooltipText + '"></span>';
            }
        }

        return result || this.emptyText;
    }
});