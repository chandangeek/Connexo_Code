/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.commandrules.CommandRuleSideMenu', {
    extend: 'Uni.view.menu.SideMenu',
    alias: 'widget.commandRuleSideMenu',
    title: Uni.I18n.translate('general.commandLimitationRule', 'MDC', 'Command limitation rule'),
    router: null,
    commandRuleName: undefined,
    initComponent: function () {
        var me = this;
        me.menuItems = [
            {
                text: Ext.isEmpty(me.commandRuleName) ? Uni.I18n.translate('general.details', 'MDC', 'Details') : me.commandRuleName,
                itemId: 'mdc-command-rule-sidemenu-overviewLink',
                href: me.router.getRoute('administration/commandrules/view').buildUrl()
            },
            {
                text: Uni.I18n.translate('general.pendingChanges', 'MDC', 'Pending changes'),
                itemId: 'mdc-command-rule-sidemenu-changesLink',
                href: me.router.getRoute('administration/commandrules/view/changes').buildUrl()
            }
        ];
        me.callParent(this);
    }
});