Ext.define('Imt.usagepointmanagement.controller.Edit', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.controller.history.Router'
    ],

    views: [
        'Imt.usagepointmanagement.view.Add'
    ],

    models: [
        'Imt.usagepointmanagement.model.UsagePoint'
    ],

    stores: [
        'Imt.servicecategories.store.ServiceCategories',
        'Imt.servicecategories.store.CAS',
        'Imt.usagepointmanagement.store.UsagePointTypes'
    ],

    refs: [
        {
            ref: 'wizard',
            selector: '#add-usage-point add-usage-point-wizard'
        }
    ],

    init: function () {
        var me = this;

        me.control({
            '#add-usage-point add-usage-point-wizard button[navigationBtn=true]': {
                click: me.moveTo
            },
            '#add-usage-point add-usage-point-navigation': {
                movetostep: me.moveTo
            }
        });
    },

    showWizard: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            widget = Ext.widget('add-usage-point', {
                itemId: 'add-usage-point',
                returnLink : router.getRoute('usagepoints').buildUrl()
            });

        me.getStore('Imt.servicecategories.store.ServiceCategories').load();
        me.getStore('Imt.usagepointmanagement.store.UsagePointTypes').load();
        me.getApplication().fireEvent('changecontentevent', widget);
        me.getWizard().loadRecord(Ext.create('Imt.usagepointmanagement.model.UsagePoint'));
    },

    moveTo: function () {
        var me = this,
            wizard = me.getWizard();

        wizard.updateRecord();
        wizard.getRecord().save();
    }
});