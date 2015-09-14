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
                    iconClass = 'uni-icon-edit';
                    tooltipText = Uni.I18n.translate('general.addedOn', 'UNI', 'Added on') + ' ' + Uni.I18n.translate('general.dateattime', 'UNI', '{0} At {1}',[Uni.DateTime.formatDateLong(date),Uni.DateTime.formatTimeLong(date)]).toLowerCase();
                    break;
                case 'EDITED':
                    iconClass = 'uni-icon-edit';
                    tooltipText = Uni.I18n.translate('general.editedOn', 'UNI', 'Edited on') + ' ' + Uni.I18n.translate('general.dateattime', 'UNI', '{0} At {1}',[Uni.DateTime.formatDateLong(date),Uni.DateTime.formatTimeLong(date)]).toLowerCase();
                    break;
                case 'ESTIMATED':
                    iconClass = 'uni-icon-edit';
                    tooltipText = Uni.I18n.translate('general.estimatedOn', 'UNI', 'Estimated on')+ ' ' + Uni.I18n.translate('general.dateattime', 'UNI', '{0} At {1}',[Uni.DateTime.formatDateLong(date),Uni.DateTime.formatTimeLong(date)]).toLowerCase();
                    break;
                case 'REMOVED':
                    iconClass = 'uni-icon-remove';
                    tooltipText = Uni.I18n.translate('general.removedOn', 'UNI', 'Removed on') + ' ' + Uni.I18n.translate('general.dateattime', 'UNI', '{0} At {1}',[Uni.DateTime.formatDateLong(date),Uni.DateTime.formatTimeLong(date)]).toLowerCase();
                    break;
            }
            if (iconClass && tooltipText) {
                result = '<span style="display: none;">' + value.flag + '</span><span class="' + iconClass + '" data-qtip="' + tooltipText + '"></span>';
            }
        }

        return result || this.emptyText;
    }
});