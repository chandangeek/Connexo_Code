/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.usagepointhistory.view.metrologyconfigurations.MetrologyConfigurationVersionPreview', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.metrology-configuration-version-preview',
    requires: [
        'Imt.usagepointhistory.view.metrologyconfigurations.MetrologyConfigurationVersionPreviewForm'
    ],
    frame: true,

    initComponent: function () {
        var me = this;
        me.items = [
            {
                xtype: 'metrology-configuration-version-preview-form',
                itemId: 'metrology-configuration-version-preview-form',
                router: me.router
            }
        ];
        me.callParent(arguments);
    },


    loadRecord: function (record) {
        var me = this,
            form = me.down('#metrology-configuration-version-preview-form');

        Ext.suspendLayouts();
        me.setTitle(record.get('period'));
        form.loadPurposes(record.get('purposesWithReadingTypes'));
        form.loadOngoingProcesses(record.get('ongoingProcesses'), record.get('ongoingProcessesNumber'));
        form.loadRecord(record);
        Ext.resumeLayouts(true);
    }
});
