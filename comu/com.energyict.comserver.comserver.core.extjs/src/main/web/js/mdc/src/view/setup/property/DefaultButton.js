/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.view.setup.property.DefaultButton', {
    extend: 'Ext.button.Button',
    alias: 'widget.defaultButton',
    border: 0,
    iconCls: 'icon-rotate-ccw3',
    style: 'background-color: transparent; padding: 5px 3px 2px 3px;',
    scale: 'small',
    action: 'delete',
    margin: '0 0 5 5',
    hidden: true,

    initComponent: function () {
        this.name = 'btn_delete_' + this.key;
        this.itemId = 'btn_delete_' + this.key;
        this.tooltip = Uni.I18n.translate('general.restoreDefaultValue', 'MDC', 'Restore to default value') + ' &quot;' + this.defaultValue + '&quot;';
        this.callParent(arguments);
    }
});