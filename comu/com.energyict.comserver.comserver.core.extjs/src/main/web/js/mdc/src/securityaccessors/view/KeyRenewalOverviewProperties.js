/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */
Ext.define('Mdc.securityaccessors.view.KeyRenewalOverviewProperties', {
    extend: 'Ext.form.Panel',
    alias: 'widget.keyRenewalOverviewProperties',
    commandTypePrefix: '',
    initComponent: function () {
        var me = this;
        me.items = [{
            xtype: 'displayfield',
            margins: '7 0 10 0',
            itemId: me.commandTypePrefix + 'previewPropertiesHeader'
        },
        {
            xtype: 'panel',
            ui: 'medium',
            itemId: me.commandTypePrefix + 'previewPropertiesPanel',
            items: [
            {
                xtype: 'property-form',
                isEdit: false,
                defaults: {
                labelWidth: 200,
                columnWidth: 0.5
                }
            }
            ]
        },
        {
            xtype: 'displayfield',
            itemId: me.commandTypePrefix + 'previewNoProperties',
            hidden: true,
            fieldLabel: ' ',
            renderer: function () {
				return '<span style="font-style:italic;color: grey;">' + Uni.I18n.translate('keyRenewal.properties.notAvailable', 'MDC', 'No attributes are available') + '</span>';
            }
        }]
        me.callParent(arguments);
    }
});