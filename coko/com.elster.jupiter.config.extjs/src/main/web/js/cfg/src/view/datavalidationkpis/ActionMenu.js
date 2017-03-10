/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Cfg.view.datavalidationkpis.ActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.cfg-data-validation-kpis-action-menu',
    initComponent: function() {
        this.items = [
            {
                text: Uni.I18n.translate('general.remove', 'CFG', 'Remove'),
                itemId: 'remove-data-validation-kpi',
                action: 'remove',
                section: this.SECTION_REMOVE
            }
        ];
        this.callParent(arguments);
    }
});
