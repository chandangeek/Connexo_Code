Ext.define('Mdc.usagepointmanagement.controller.History', {
    extend: 'Ext.app.Controller',
    requires: [
        'Mdc.usagepointmanagement.model.UsagePoint',
        'Mdc.usagepointmanagement.model.MetrologyConfigurationVersion',
        'Mdc.usagepointmanagement.model.UsagePointWithVersion',
        'Ext.container.Container'
    ],
    stores: [
        'Mdc.usagepointmanagement.store.MetrologyConfigurationVersions'
    ],
    views: [
        'Mdc.usagepointmanagement.view.history.Setup',
        'Mdc.usagepointmanagement.view.history.AddMetrologyConfigurationVersion'
    ],
    refs: [
        {ref: 'metrologyConfigurationTab', selector: 'metrology-configuration-history-tab'},
        {ref: 'metrologyConfigurationActionMenu', selector: 'metrology-configuration-versions-action-menu'},
        {ref: 'addVersionPanel', selector: 'add-metrology-configuration-version'},
        {ref: 'metrologyConfigGrid', selector: '#metrology-configuration-history-grid-id'}
    ],

    init: function () {
        this.control({
        });
    }


})
;

