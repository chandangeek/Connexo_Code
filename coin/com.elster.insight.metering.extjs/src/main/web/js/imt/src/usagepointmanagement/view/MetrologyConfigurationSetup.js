/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointmanagement.view.MetrologyConfigurationSetup', {
    extend: 'Uni.view.container.ContentContainer',
    alias: 'widget.up-metrology-configuration-setup',
    itemId: 'up-metrology-configuration-setup',
    requires: [
        'Imt.metrologyconfiguration.view.MetrologyConfigurationDetailsForm'
    ],
    router: null,
    content: [
        {
            xtype: 'panel',
            ui: 'large',
            itemId: 'upMetrologyConfigurationSetupPanel',
            layout: {
                type: 'fit',
                align: 'stretch'
            }
        }
    ],

    initComponent: function () {
        var me = this,
            panel = me.content[0];
        this.callParent(arguments);

        me.down('#upMetrologyConfigurationSetupPanel').add(
            {
                xtype: 'panel',
                layout: {
                    type: 'hbox'
                },
                items: [
                   {
                        xtype: 'panel',
                        title: Uni.I18n.translate('metrologyconfiguration.attributes', 'IMT', 'Metrology configuration attributes'),
                        ui: 'tile',
                        itemId: 'up-metrology-configuration-attributes-panel',
                        router: me.router
                    }
                ]
            }
        );
    }
});