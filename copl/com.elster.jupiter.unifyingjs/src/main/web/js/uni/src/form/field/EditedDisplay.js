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
                    tooltipText = Uni.I18n.translate('general.addedOnX', 'UNI', 'Added on {0}',
                        Uni.I18n.translate('general.dateAtTime', 'UNI', '{0} at {1}',
                            [Uni.DateTime.formatDateLong(date), Uni.DateTime.formatTimeLong(date)]
                        )
                    );
                    break;
                case 'EDITED':
                    iconClass = 'uni-icon-edit';
                    tooltipText = Uni.I18n.translate('general.editedOnX', 'UNI', 'Edited on {0}',
                        Uni.I18n.translate('general.dateAtTime', 'UNI', '{0} at {1}',
                            [Uni.DateTime.formatDateLong(date), Uni.DateTime.formatTimeLong(date)]
                        )
                    );
                    break;
                case 'ESTIMATED':
                    iconClass = 'uni-icon-edit';
                    tooltipText = Uni.I18n.translate('general.estimatedOnX', 'UNI', 'Estimated on {0}',
                        Uni.I18n.translate('general.dateAtTime', 'UNI', '{0} at {1}',
                            [Uni.DateTime.formatDateLong(date), Uni.DateTime.formatTimeLong(date)]
                        )
                    );
                    break;
                case 'REMOVED':
                    iconClass = 'uni-icon-remove';
                    tooltipText = Uni.I18n.translate('general.removedOnX', 'UNI', 'Removed on {0}',
                        Uni.I18n.translate('general.dateAtTime', 'UNI', '{0} at {1}',
                            [Uni.DateTime.formatDateLong(date), Uni.DateTime.formatTimeLong(date)]
                        )
                    );
                    break;
            }
            if (iconClass && tooltipText) {
                result = '<span style="display: none;">' + value.flag + '</span><span class="' + iconClass + '" data-qtip="' + tooltipText + '"></span>';
            }
        }

        return result || this.emptyText;
    }
});