Ext.define('Imt.servicecategories.controller.ServiceCategories', {
    extend: 'Ext.app.Controller',
    stores: [
        'Imt.servicecategories.store.ServiceCategories',
        'Imt.servicecategories.store.CAS'
    ],
    views: [
        'Imt.servicecategories.view.Setup'
    ],

    refs: [
        {
            ref: 'casPanel',
            selector: '#service-categories-setup #service-categories-cas-panel'
        },
        {
            ref: 'casPreview',
            selector: '#service-categories-setup #cas-preview'
        }
    ],

    init: function () {
        var me = this;

        me.control({
            '#service-categories-setup #service-categories-grid': {
                select: me.showServiceCategoryPreview
            },
            '#service-categories-setup #cas-grid': {
                select: me.showServiceCasPreview
            }
        });
    },

    showOverview: function () {
        var me = this,
            widget = Ext.widget('service-categories-setup', {
                itemId: 'service-categories-setup'
            });

        me.getApplication().fireEvent('changecontentevent', widget);
        me.getStore('Imt.servicecategories.store.ServiceCategories').load();
    },

    showServiceCategoryPreview: function (selectionModel, record) {
        var me = this,
            casPanel = me.getCasPanel(),
            store = me.getStore('Imt.servicecategories.store.CAS');

        casPanel.setTitle(record.get('displayName'));
        store.getProxy().setUrl(record.getId());
        store.load();
    },

    showServiceCasPreview: function (selectionModel, record) {
        var me = this,
            casPreview = me.getCasPreview();

        Ext.suspendLayouts();
        casPreview.setTitle(record.get('name'));
        casPreview.loadRecord(record);
        Ext.resumeLayouts(true);
    }
});