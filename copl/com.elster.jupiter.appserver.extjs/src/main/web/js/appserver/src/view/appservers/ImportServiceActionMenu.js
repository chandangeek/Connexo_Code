/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.view.appservers.ImportServiceActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.apr-import-services-action-menu',
    initComponent: function () {
        this.items =
            [
                {
                    itemId: 'remove-import-service',
                    text: Uni.I18n.translate('general.remove', 'APR', 'Remove'),
                    section: this.SECTION_REMOVE
                }
            ];
        this.callParent(arguments);
    }
});