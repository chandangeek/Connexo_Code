Ext.define('Mdc.controller.setup.EstimationDeviceConfigurations', {
    extend: 'Ext.app.Controller',

    views: [
        'Mdc.view.setup.estimationdeviceconfigurations.Setup',
        'Mdc.view.setup.estimationdeviceconfigurations.Add'
    ],

    stores: [
        'Mdc.store.EstimationDeviceConfigurations',
        'Mdc.store.EstimationDeviceConfigurationsBuffered'
    ],

    models: [
        'Mdc.model.EstimationDeviceConfiguration',
        'Mdc.model.EstimationRuleSet'
    ],

    refs: [
        {
            ref: 'page',
            selector: 'estimation-deviceconfigurations-setup'
        },
        {
            ref: 'addPage',
            selector: 'estimation-deviceconfigurations-add'
        }
    ],

    ruleSetId: null,

    init: function () {
        var me = this;
        me.control({
            'estimation-deviceconfigurations-setup estimation-deviceconfigurations-grid': {
                select: me.showEstimationDeviceConfigurationPreview
            },
            'estimation-deviceconfigurations-add #add-deviceconfigurations-grid': {
                allitemsadd: me.onAllDeviceConfigurationsAdd,
                selecteditemsadd: me.onSelectedDeviceConfigurationsAdd
            },
            'estimation-rule-set-side-menu[sharedForMdc=true]': {
                beforerender: me.onEstimationRuleSetMenuBeforeRender
            }
        });

        var menu = Ext.ComponentQuery.query('estimation-rule-set-side-menu[sharedForMdc=true]')[0];
        if (menu && menu.rendered) {
            me.onEstimationRuleSetMenuBeforeRender(menu);
        }
    },

    onEstimationRuleSetMenuBeforeRender: function (menu) {
        var me = this;
        menu.add(
            {
                text: Uni.I18n.translate('estimationDeviceConfigurations.deviceConfigurations', 'MDC', 'Device configurations'),
                itemId: 'estimation-device-configurations-link',
                href: me.getController('Uni.controller.history.Router').getRoute('administration/estimationrulesets/estimationruleset/deviceconfigurations').buildUrl()
            }
        );
    },


    showEstimationDeviceConfigurations: function (ruleSetId) {
        var me = this,
            model = me.getModel('Mdc.model.EstimationRuleSet'),
            router = me.getController('Uni.controller.history.Router'),
            store = me.getStore('Mdc.store.EstimationDeviceConfigurations'),
            pageView = Ext.ComponentQuery.query('viewport > #contentPanel')[0],
            view;

        pageView.setLoading(true);
        store.getProxy().setUrl(router.arguments);
        view = Ext.widget('estimation-deviceconfigurations-setup', {
            router: router
        });

        model.load(ruleSetId, {
            success: function (ruleSetRecord) {
                me.getApplication().fireEvent('loadEstimationRuleSet', ruleSetRecord);
                view.down('#estimation-rule-set-link').setText(ruleSetRecord.get('name'));
                me.getApplication().fireEvent('changecontentevent', view);
            },
            callback: function () {
                pageView.setLoading(false);
            }
        });
    },

    showEstimationDeviceConfigurationPreview: function (selectionModel, record, index) {
        var me = this,
            page = me.getPage(),
            preview = page.down('estimation-deviceconfigurations-preview'),
            previewForm = page.down('#estimation-deviceconfigurations-preview-form'),
            dataSourcesList = '',
            registerUrl,
            loadProfileUrl;

        Ext.suspendLayouts();
        preview.setTitle(record.get('name'));
        previewForm.loadRecord(record);
        previewForm.down('#data-sources').removeAll();

        registerUrl = '#/administration/devicetypes/' + encodeURIComponent(record.get('deviceTypeId')) + '/deviceconfigurations/' + encodeURIComponent(record.get('id')) + '/registerconfigurations';
        dataSourcesList += '<a href="' + registerUrl + '">' + record.get('registerCount') + ' ' + Uni.I18n.translate('estimationDeviceConfigurations.registerConfigurations', 'MDC', 'register configurations') + '</a><br/>';

        loadProfileUrl = '#/administration/devicetypes/' + encodeURIComponent(record.get('deviceTypeId')) + '/deviceconfigurations/' + encodeURIComponent(record.get('id')) + '/loadprofiles';
        dataSourcesList += '<a href="' + loadProfileUrl + '">' + record.get('loadProfileCount') + ' ' + Uni.I18n.translate('estimationDeviceConfigurations.loadProfileConfigurations', 'MDC', 'load profile configurations') + '</a><br/>';

        previewForm.down('#data-sources').add({
            xtype: 'displayfield',
            value: dataSourcesList
        });
        Ext.resumeLayouts(true);
    },

    showAddEstimationDeviceConfigurations: function (ruleSetId) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            view = Ext.widget('estimation-deviceconfigurations-add', {
                router: router
            }),
            store = me.getStore('Mdc.store.EstimationDeviceConfigurationsBuffered'),
            model = me.getModel('Mdc.model.EstimationRuleSet');

        store.getProxy().setUrl(router.arguments);
        me.ruleSetId = ruleSetId;
        model.load(ruleSetId, {
            success: function (ruleSet) {
                me.getApplication().fireEvent('loadEstimationRuleSet', ruleSet);
                me.getApplication().fireEvent('changecontentevent', view);
                store.data.clear();
                store.loadPage(1);
            }
        });
    },

    onAllDeviceConfigurationsAdd: function () {
        this.addDeviceConfigurations(true, []);
    },

    onSelectedDeviceConfigurationsAdd: function (selection) {
        this.addDeviceConfigurations(false, selection);
    },

    addDeviceConfigurations: function (allPressed, selection) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            url = '/api/dtc/estimationrulesets/' + me.ruleSetId + '/deviceconfigurations',
            ids = [];

        if (!allPressed) {
            Ext.Array.each(selection, function (item) {
                ids.push({
                    id: item.get('id'),
                    version: item.get('version')
                });
            });
        }

        me.getAddPage().setLoading();
        Ext.Ajax.request({
            url: url,
            method: 'POST',
            timeout: 120000,
            params: {
                all: allPressed
            },
            jsonData: Ext.encode(ids),
            success: function () {
                router.getRoute('administration/estimationrulesets/estimationruleset/deviceconfigurations').forward();
                var quantityOfConfigurations = allPressed ? 'all' : selection.length;
                var message = Uni.I18n.translatePlural(
                    'estimationDeviceConfigurations.addSuccess',
                    quantityOfConfigurations,
                    'MDC',
                    'Successfully added {0} device configurations',
                    'Successfully added {0} device configuration',
                    'Successfully added {0} device configurations'
                );
                me.getApplication().fireEvent('acknowledge', message);
            },
            callback: function () {
                me.getAddPage().setLoading(false);
            }
        });
    }
});