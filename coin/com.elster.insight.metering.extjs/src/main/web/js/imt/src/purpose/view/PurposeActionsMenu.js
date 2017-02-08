/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.purpose.view.PurposeActionsMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.purpose-actions-menu',
    plain: true,
    border: false,
    shadow: false,

    items: [
        {
            text: Uni.I18n.translate('general.validateNow', 'IMT', 'Validate now'),
            action: 'validateNow',
            itemId: 'validate-now'
        }
    ]
});

