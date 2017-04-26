/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 * @class Uni.form.field.EditedDisplay
 */
Ext.define('Uni.form.field.EditedDisplay', {
    extend: 'Ext.form.field.Display',
    xtype: 'edited-displayfield',
    name: 'editedDate',
    emptyText: '',

    renderer: function (value, cell, record) {
        var result,
            date,
            formattedDate,
            iconClass,
            tooltipText;

        if (value) {
            date = Ext.isDate(value.date) ? value.date : new Date(value.date);
            formattedDate = Uni.I18n.translate('general.dateAtTime', 'UNI', '{0} at {1}',
                [Uni.DateTime.formatDateLong(date), Uni.DateTime.formatTimeLong(date)]
            );
            switch (value.flag) {
                case 'ADDED':
                    iconClass = 'icon-pencil4';
                    tooltipText = value.date === null
                        ? Uni.I18n.translate('general.addedOnXx', 'UNI', 'Added')
                        : Uni.I18n.translate('general.addedOnX', 'UNI', 'Added on {0}', formattedDate);
                    break;
                case 'EDITED':
                    iconClass = 'icon-pencil4';
                    tooltipText = value.date === null
                        ? Uni.I18n.translate('general.editedOnXx', 'UNI', 'Edited')
                        : Uni.I18n.translate('general.editedOnX', 'UNI', 'Edited on {0}', formattedDate);
                    tooltipText += record.get('commentValue')
                        ? ' ' + Uni.I18n.translate('general.estimationCommentWithComment', 'UNI', 'Estimation comment: {0}', record.get('commentValue'))
                        : '';
                    break;
                case 'ESTIMATED':
                    iconClass = 'icon-pencil4';
                    tooltipText = value.date === null
                        ? Uni.I18n.translate('general.estimatedOnXx', 'UNI', 'Estimated')
                        : Uni.I18n.translate('general.estimatedOnX', 'UNI', 'Estimated on {0}', formattedDate);
                    break;
                case 'REMOVED':
                    iconClass = 'icon-cancel-circle';
                    tooltipText = value.date === null
                        ? Uni.I18n.translate('general.removedOnXx', 'UNI', 'Removed')
                        : Uni.I18n.translate('general.removedOnX', 'UNI', 'Removed on {0}', formattedDate);
                    break;
                case 'RESET':
                    iconClass = 'icon-cancel-circle';
                    tooltipText = value.date === null
                        ? Uni.I18n.translate('general.restoredOnXx', 'UNI', 'Restored')
                        : Uni.I18n.translate('general.restoredOnX', 'UNI', 'Restored on {0}', formattedDate);
                    break;
            }
            if (iconClass && tooltipText) {
                result = '<span style="display: none;">' + value.flag + '</span><span class="' + iconClass + '" data-qtip="' + tooltipText + '"></span>';
            }
        }

        return result || this.emptyText;
    }
});