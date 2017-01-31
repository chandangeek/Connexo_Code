/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Apr.view.appservers.WebserviceEndpointActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.apr-webservices-action-menu',
    initComponent: function() {
        this.items =  [
            {
                itemId: 'remove-webservice',
                text: Uni.I18n.translate('general.remove', 'APR', 'Remove'),
                section: this.SECTION_REMOVE
            }
        ];
        this.callParent(arguments);
    }
});