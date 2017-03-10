/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Est.estimationtasks.view.LogSideMenu', {
    extend: 'Uni.view.menu.SideMenu',
    alias: 'widget.estimationtasks-log-menu',
    router: null,
    title: Uni.I18n.translate('general.estimationTask', 'EST', 'Estimation task'),
    objectType: Uni.I18n.translate('general.estimationTask', 'EST', 'Estimation task'),

    initComponent: function () {
        var me = this;
        me.callParent(me);
        me.add({
                text: Uni.I18n.translate('general.details', 'EST', 'Details'),
                itemId: 'estimationtasks-overview-link',
                href: me.router.getRoute('administration/estimationtasks/estimationtask').buildUrl({taskId: me.taskId})
        });

        me.add(
            {
                text: Uni.I18n.translate('estimationtasks.general.log', 'EST', 'Log'),
                itemId: 'estimationtasks-log-link',
                href: me.router.getRoute('administration/estimationtasks/estimationtask/history').buildUrl({occurenceId: me.occurenceId})
            }
        );
    }
});


