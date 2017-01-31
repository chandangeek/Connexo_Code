/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.metrologyconfiguration.view.ActionMenu', {
    extend: 'Uni.view.menu.ActionsMenu',
    alias: 'widget.metrologyConfigurationActionMenu',
    initComponent: function () {
        this.items = [
            {
                itemId: 'edit',
                text: Uni.I18n.translate('general.menu.edit', 'IMT', 'Edit'),
                action: 'editMetrologyConfiguration',
                section: this.SECTION_EDIT
            },
            {
                itemId: 'remove',
                text: Uni.I18n.translate('general.menu.remove', 'IMT', 'Remove'),
                action: 'removeMetrologyConfiguration',
                section: this.SECTION_REMOVE
            }
        ];
        this.callParent(arguments);
    }
});
