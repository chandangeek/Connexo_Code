/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.metrologyconfiguration.view.MetrologyConfigurationPurposes', {
    extend: 'Uni.view.container.PreviewContainer',
    requires: [
        'Imt.metrologyconfiguration.view.MetrologyConfigurationPurposesGrid',
        'Imt.metrologyconfiguration.view.MetrologyConfigurationPurposeDetailsForm',
        'Uni.view.notifications.NoItemsFoundPanel'
    ],
    alias: 'widget.metrology-config-purposes',
    metrologyConfig: null,

    emptyComponent: {
        xtype: 'no-items-found-panel',
        itemId: 'no-metrology-config-purposes-found-panel',
        title: Uni.I18n.translate('metrologyConfigPurposes.empty.title', 'IMT', 'No purposes found'),
        reasons: [
            Uni.I18n.translate('purposes.empty.list.item', 'IMT', 'No purposes have been added yet.')
        ]
        // out of scope CXO-633
        //stepItems: [
        //    {
        //        xtype: 'button',
        //        text: Uni.I18n.translate('metrologyConfigPurposes.add', 'IMT', 'Add purpose'),
        //        privileges: Imt.privileges.MetrologyConfig.admin,
        //        action: 'addPurpose'
        //    }
        //]
    },
    previewComponent: {
        xtype: 'metrology-config-purpose-detail-form',
        itemId: 'purpose-preview',
        frame: true,
        title: ' '
    },

    initComponent: function () {
        var me = this,
            metrologyContracts = me.metrologyConfig.getReadingTypeDeliverablesStore();

        me.grid = {
            xtype: 'metrology-config-purposes-grid',
            itemId: 'purposes-grid',
            store: metrologyContracts
        };

        me.on('afterrender', function () {
            metrologyContracts.fireEvent('load');
        }, me, {single: true});

        me.callParent(arguments);
    }
});