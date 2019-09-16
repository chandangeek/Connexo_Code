/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Usr.view.user.util.UsersNotFoundPanel', {

    extend: 'Uni.view.notifications.NoItemsFoundPanel',

    alias: 'widget.users-not-found-panel',

    title: Uni.I18n.translate('devicegroupfromissues.wizard.step.selectDevices.grid.noDevicesFound', 'ISU', 'No users found'),

    reasons: [
        Uni.I18n.translate('devicegroupfromissues.wizard.step.selectDevices.grid.reasonOne', 'ISU', 'There are no users in the system.'),
        Uni.I18n.translate('devicegroupfromissues.wizard.step.selectDevices.grid.reasonTwo', 'ISU', 'No users comply with the filter.')
    ],

    margin: '16 0 24 0'

});