Ext.define('Mdc.metrologyconfiguration.controller.AddView', {
    extend: 'Ext.app.Controller',
    requires: [
        'Uni.controller.history.Router'
    ],

    models: [
        'Mdc.metrologyconfiguration.model.MetrologyConfiguration'
    ],

    stores: [
        'Mdc.metrologyconfiguration.store.ServiceCategories'
    ],

    views: [
        'Uni.view.container.ContentContainer',
        'Mdc.metrologyconfiguration.view.AddForm'
    ],

    refs: [
        {ref: 'page', selector: '#metrology-configurations-add-view'},
        {ref: 'form', selector: '#metrology-configurations-add-view #metrology-configurations-add-form'}
    ],

    init: function () {
        var me = this;

        me.control({
            '#metrology-configurations-add-view #mc-add-button': {
                click: me.saveMetrologyConfiguration
            }
        });
    },

    showForm: function () {
        var me = this,
            app = me.getApplication(),
            router = me.getController('Uni.controller.history.Router'),
            widget = Ext.widget('contentcontainer', {
                itemId: 'metrology-configurations-add-view',
                content: [
                    {
                        xtype: 'metrology-configurations-add-form',
                        itemId: 'metrology-configurations-add-form',
                        ui: 'large',
                        title: Uni.I18n.translate('general.addMetrologyConfiguration', 'MDC', 'Add metrology configuration'),
                        returnLink: router.getRoute('administration/metrologyconfiguration').buildUrl()
                    }
                ]
            });

        me.getStore('Mdc.metrologyconfiguration.store.ServiceCategories').load();
        me.getForm().loadRecord(Ext.create('Mdc.metrologyconfiguration.model.MetrologyConfiguration'));
        app.fireEvent('changecontentevent', widget);
    },

    saveMetrologyConfiguration: function () {
        var me = this,
            page = me.getPage(),
            form = me.getForm(),
            baseForm = form.getForm(),
            warning = form.down('#mc-add-warning');

        Ext.suspendLayouts();
        warning.hide();
        baseForm.clearInvalid();
        Ext.resumeLayouts(true);
        form.updateRecord();
        form.getRecord().save({
            backUrl: form.returnLink,
            callback: function (record, operation, success) {
                var responseText = Ext.decode(operation.response.responseText, true);

                page.setLoading(false);
                if (success) {
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('metrologyconfiguration.addMetrologyConfigurationSuccess', 'MDC', 'Metrology configuration added'));
                    if (page.rendered) {
                        window.location.href = form.returnLink;
                    }
                } else {
                    if (page.rendered && responseText && responseText.errors) {
                        warning.show();
                        baseForm.markInvalid(responseText.errors);
                    }
                }
            }
        });
    }
});