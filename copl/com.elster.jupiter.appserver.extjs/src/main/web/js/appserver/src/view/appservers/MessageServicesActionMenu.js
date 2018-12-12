/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.view.appservers.MessageServicesActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.message-services-action-menu',

    initComponent: function () {
        this.items =
            [
                {
                    itemId: 'remove-message-service',
                    text: Uni.I18n.translate('general.remove', 'APR', 'Remove'),
                    action: 'removeMessageService',
                    section: this.SECTION_REMOVE
                }
            ];
        this.callParent(arguments);
    }
});