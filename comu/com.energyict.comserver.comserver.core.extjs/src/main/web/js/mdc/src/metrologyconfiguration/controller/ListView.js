Ext.define('Mdc.metrologyconfiguration.controller.ListView', {
    extend: 'Ext.app.Controller',

    stores: [
        'Mdc.metrologyconfiguration.store.MetrologyConfigurations'
    ],

    views: [
        'Mdc.metrologyconfiguration.view.ListView'
    ],

    refs: [
        {ref: 'preview', selector: '#metrology-configurations-list-view #metrology-configuration-preview'}
    ],

    init: function () {
        var me = this;

        me.control({
            '#metrology-configurations-list-view #metrology-configurations-grid': {
                select: me.showPreview
            }
        });
    },

    showList: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            widget = Ext.widget('metrology-configurations-list-view', {
                itemId: 'metrology-configurations-list-view',
                router: router
            });

        me.getApplication().fireEvent('changecontentevent', widget);
    },

    showPreview: function (selectionModel, record) {
        var me = this,
            preview = me.getPreview();

        Ext.suspendLayouts();
        preview.setTitle(record.get('name'));
        preview.loadRecord(record);
        Ext.resumeLayouts(true);
    }
});