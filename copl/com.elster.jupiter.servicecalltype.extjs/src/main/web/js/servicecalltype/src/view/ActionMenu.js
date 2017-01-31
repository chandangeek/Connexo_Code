/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Sct.view.ActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.sct-action-menu',
    initComponent: function () {
        this.items = [
            {
                itemId: 'change-log-level-sct',
                text: Uni.I18n.translate('general.changeLogLevel', 'SCT', 'Change log level'),
                privileges: Sct.privileges.ServiceCallType.admin,
                action: 'changeLogLevel',
                section: this.SECTION_ACTION
            }
        ];
        this.callParent(arguments);
    }
});