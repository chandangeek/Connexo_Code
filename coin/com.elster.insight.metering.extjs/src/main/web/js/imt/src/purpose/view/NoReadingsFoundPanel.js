/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.purpose.view.NoReadingsFoundPanel', {
    extend: 'Uni.view.notifications.NoItemsFoundPanel',
    alias: 'widget.no-readings-found-panel',
    title: Uni.I18n.translate('readings.list.empty', 'IMT', 'No data is available'),
    reasons: [
        Uni.I18n.translate('readings.list.reason1x', 'IMT', 'No metrology configurations in the specified period of time'),
        Uni.I18n.translate('readings.list.reason4', 'IMT', 'No data matching to the filters')
    ]
});