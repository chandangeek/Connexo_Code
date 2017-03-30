/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.comtasks.SideMenu', {
    extend: 'Uni.view.menu.SideMenu',
    alias: 'widget.comTaskSideMenu',
    title: Uni.I18n.translate('general.communicationTask', 'MDC', 'Communication task'),
    objectType: Uni.I18n.translate('general.communicationTask', 'MDC', 'Communication task'),
    router: null,
    initComponent: function () {
        var me = this;
        me.menuItems = [
            {
                text: Uni.I18n.translate('general.details', 'MDC', 'Details'),
                itemId: 'mdc-comtask-sidemenu-overviewLink',
                href: me.router.getRoute('administration/communicationtasks/view').buildUrl()
            },
            {
                text: Uni.I18n.translate('comtask.actions', 'MDC', 'Actions'),
                itemId: 'mdc-comtask-sidemenu-actionsLink',
                href: me.router.getRoute('administration/communicationtasks/view/actions').buildUrl()
            },
            {
                text: Uni.I18n.translate('comtask.message.categories', 'MDC', 'Command categories'),
                itemId: 'mdc-comtask-sidemenu-commandCategoriesLink',
                href: me.router.getRoute('administration/communicationtasks/view/commandcategories').buildUrl()
            }
        ];
        me.callParent(this);
    }
});