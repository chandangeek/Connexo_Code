/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointgroupsfromissues.view.grid.util.UsagePointsNotFoundPanel', {

    extend: 'Uni.view.notifications.NoItemsFoundPanel',

    alias: 'widget.usage-points-not-found-panel',

    title: Uni.I18n.translate('usagepointgroupfromissues.wizard.step.selectUsagePoints.grid.noUsagePointsFound', 'IMT', 'No usage points found'),

    reasons: [
        Uni.I18n.translate('usagepointgroupfromissues.wizard.step.selectUsagePoints.grid.reasonOne', 'IMT', 'There are no usage points in the system.'),
        Uni.I18n.translate('usagepointgroupfromissues.wizard.step.selectUsagePoints.grid.reasonTwo', 'IMT', 'No usage points comply with the filter.')
    ],

    margin: '16 0 24 0'

});