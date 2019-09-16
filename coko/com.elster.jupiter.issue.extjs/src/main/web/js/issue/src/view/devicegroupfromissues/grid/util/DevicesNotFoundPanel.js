/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Isu.view.devicegroupfromissues.grid.util.DevicesNotFoundPanel', {

    extend: 'Uni.view.notifications.NoItemsFoundPanel',

    alias: 'widget.devices-not-found-panel',

    title: Uni.I18n.translate('devicegroupfromissues.wizard.step.selectDevices.grid.noDevicesFound', 'ISU', 'No devices found'),

    reasons: [
        Uni.I18n.translate('devicegroupfromissues.wizard.step.selectDevices.grid.reasonOne', 'ISU', 'There are no devices in the system.'),
        Uni.I18n.translate('devicegroupfromissues.wizard.step.selectDevices.grid.reasonTwo', 'ISU', 'No devices comply with the filter.')
    ],

    margin: '16 0 24 0'

});