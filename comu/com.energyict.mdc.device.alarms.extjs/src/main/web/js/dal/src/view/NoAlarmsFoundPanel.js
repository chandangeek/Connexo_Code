/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dal.view.NoAlarmsFoundPanel', {
    extend: 'Uni.view.notifications.NoItemsFoundPanel',
    alias: 'widget.no-alarms-found-panel',
    title: Uni.I18n.translate('alarms.empty.title', 'DAL', 'No alarms found'),
    reasons: [
        Uni.I18n.translate('workspace.alarms.empty.list.item1', 'DAL', 'No alarm creation rules have been defined yet.'),
        Uni.I18n.translate('workspace.alarms.empty.list.item2', 'DAL', "The current alarm creation rules haven't generated any alarms."),
        Uni.I18n.translate('workspace.alarms.empty.list.item3', 'DAL', 'No alarms comply to the filter.')
    ]
});