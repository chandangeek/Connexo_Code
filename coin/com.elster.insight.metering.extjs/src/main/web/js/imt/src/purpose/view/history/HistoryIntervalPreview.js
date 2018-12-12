/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */


Ext.define('Imt.purpose.view.history.HistoryIntervalPreview', {
    extend: 'Imt.purpose.view.IntervalReadingPreview',
    alias: 'widget.history-interval-preview',

    /**
     * @override
     */
    getGeneralItems: function () {
        return [
            {
                fieldLabel: Uni.I18n.translate('general.interval', 'IMT', 'Interval'),
                name: 'interval',
                itemId: 'history-preview-interval-field',
                renderer: Imt.purpose.util.DataFormatter.formatIntervalLong
            },
            {
                fieldLabel: Uni.I18n.translate('historyGrid.changedOn', 'IMT', 'Changed on'),
                name: 'reportedDateTime',
                itemId: 'history-preview-changedOn-field',
                renderer: function (value) {
                    return value ? Uni.DateTime.formatDateTimeLong(new Date(value)) : '-';
                }
            },
            {
                fieldLabel: Uni.I18n.translate('historyGrid.changedBy', 'IMT', 'Changed by'),
                name: 'userName',
                itemId: 'history-preview-changedBy-field',
                htmlEncode: false
            }
        ];
    }
});