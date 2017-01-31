/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Scs.view.ActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.scs-action-menu',
    initComponent: function() {
        this.items = [
            {
                itemId: 'cancel-scs',
                text: Uni.I18n.translate('general.cancel', 'SCS', 'Cancel'),
                privileges: Scs.privileges.ServiceCall.admin,
                action: 'cancel',
                section: this.SECTION_ACTION
            }
        ];
        this.callParent(arguments);
    }
});