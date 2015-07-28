Ext.define('Ddv.controller.ValidationOverview', {
    extend: 'Ext.app.Controller',

    requires: [
        //'Dsh.model.Filterable',
        //'Dsh.view.widget.HeaderSection'
    ],

    views: [
        'Ddv.view.ValidationOverview'
    ],
    stores:[
        'Ddv.store.ValidationOverview'
    ],

    models: [
        'Ddv.model.ValidationOverview'//,
        //'Ddv.model.ValidationOverviewResult',

    ],

    refs: [
        {ref: 'validationOverview', selector: 'ddv-validation-overview'},
        {ref: 'header', selector: '#header-section'},
        {ref: 'labelGroupResult', selector: '#lbl-group-result'}
    ],

    init: function () {
        this.control({
            'ddv-validation-overview #refresh-btn': {
                click: this.showOverview
            }
        });
    },

    showOverview: function () {
        var me = this,
            store = me.getStore('Ddv.store.ValidationOverview'),
            router = this.getController('Uni.controller.history.Router');

        if (router.filter.get('deviceGroup') == '') {
            me.getApplication().fireEvent('changecontentevent',
                Ext.widget('ddv-validation-overview', {
                    router: router,
                    hiddenGrid: true,
                    hiddenNoGroup: false
                }));


            me.getHeader().down('#last-updated-field').setValue(Ext.String.format(Uni.I18n.translate('validation.validationOverview.lastUpdated', 'MDC', 'Last updated at {0}'), Uni.DateTime.formatTimeShort(new Date())));
            return;
        }

        store.getProxy().setUrl(router.filter);

        //me.getValidationResults().setLoading();
        store.load(function(records) {
                // me.getValidationResults().setLoading(false);
                me.getApplication().fireEvent('changecontentevent',
                    Ext.widget('ddv-validation-overview',{
                            router: router,
                            hiddenGrid: false,
                            hiddenNoGroup: true
                    }));
                me.getHeader().down('#last-updated-field').setValue(Ext.String.format(Uni.I18n.translate('validation.validationOverview.lastUpdated', 'MDC', 'Last updated at {0}'), Uni.DateTime.formatTimeShort(new Date())));
            }
        );
    }
});
