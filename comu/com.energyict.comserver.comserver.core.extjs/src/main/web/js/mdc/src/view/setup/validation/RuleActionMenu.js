/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.validation.RuleActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.validation-rule-actionmenu',
    initComponent: function () {
        this.items = [
            {
                text: Uni.I18n.translate('general.view', 'MDC', 'View'),
                itemId: 'viewRule',
                action: 'viewRule',
                section: this.SECTION_VIEW
            }
        ];
        this.callParent(arguments);
    }
});
