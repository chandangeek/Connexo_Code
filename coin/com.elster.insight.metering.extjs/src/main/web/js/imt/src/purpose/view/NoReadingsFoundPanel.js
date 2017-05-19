/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.purpose.view.NoReadingsFoundPanel', {
    extend: 'Uni.view.notifications.NoItemsFoundPanel',
    alias: 'widget.no-readings-found-panel',
    title: Uni.I18n.translate('readings.list.empty', 'IMT', 'No data is available'),
    reasons: [
        Uni.I18n.translate('readings.list.reason.1', 'IMT', 'No data has been provided for this output.'),
        Uni.I18n.translate('readings.list.reason.2', 'IMT', 'No devices have been linked to this usage point for the specified period.'),
        Uni.I18n.translate('readings.list.reason.3', 'IMT', 'No data complies with the filter.'),
        Uni.I18n.translate('readings.list.reason1x', 'IMT', 'No metrology configurations in the specified period of time')
    ]
});