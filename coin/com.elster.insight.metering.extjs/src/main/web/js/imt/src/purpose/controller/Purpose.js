/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Imt.purpose.controller.Purpose', {
    extend: 'Ext.app.Controller',

    requires: [
        'Uni.controller.history.Router',
        'Imt.purpose.view.Outputs',
        'Imt.purpose.store.Outputs',
        'Imt.purpose.view.OutputChannelMain',
        'Uni.store.DataIntervalAndZoomLevels'
    ],

    stores: [
        'Imt.purpose.store.Outputs',
        'Imt.purpose.store.Readings',
        'Uni.store.DataIntervalAndZoomLevels',
        'Imt.purpose.store.RegisterReadings',
        'Imt.usagepointmanagement.store.UsagePointTypes',
        'Imt.purpose.store.ValidationTasks',
        'Imt.purpose.store.EstimationTasks',
        'Imt.usagepointmanagement.store.Periods',
        'Imt.purpose.store.EstimationRules',
        'Imt.purpose.store.OutputValidationConfiguration',
        'Imt.purpose.store.OutputEstimationConfiguration'
    ],

    models: [
        'Imt.purpose.model.Output',
        'Imt.purpose.model.Reading',
        'Imt.usagepointmanagement.model.ValidationInfo',
        'Imt.usagepointmanagement.model.SuspectReason',
        'Imt.usagepointmanagement.model.Purpose',
        'Imt.usagepointmanagement.model.UsagePoint',
        'Imt.purpose.model.OutputValidationConfiguration',
        'Imt.purpose.model.OutputEstimationConfiguration'
    ],

    views: [
        'Imt.purpose.view.Outputs',
        'Imt.purpose.view.OutputChannelMain',
        'Imt.purpose.view.ValidationStatusForm',
        'Imt.purpose.view.ValidationDate',
        'Uni.view.window.Confirmation',
        'Ext.ProgressBar',
        'Cfg.configuration.view.RuleWithAttributesEdit'
    ],

    refs: [
        {
            ref: 'readingPreviewPanel',
            selector: 'output-channel-main reading-preview'
        },
        {
            ref: 'purposePage',
            selector: '#purpose-outputs'
        },
        {
            ref: 'outputPreview',
            selector: '#purpose-outputs #output-preview'
        }
    ],

    hasEstimationRule: false,

    init: function () {
        this.control({
            'output-channel-main #readings-graph': {
                resize: this.onGraphResize
            },
            'purpose-outputs purpose-actions-menu': {
                click: this.chooseAction
            },
            '#purpose-outputs #outputs-list': {
                select: this.showOutputPreview
            }
        });
    },

    loadOutputs: function (usagePointId, purposeId, callback) {
        var me = this,
            outputsStore = me.getStore('Imt.purpose.store.Outputs');

        outputsStore.getProxy().extraParams = {usagePointId: usagePointId, purposeId: purposeId};
        outputsStore.load(callback);
    },

    showOutputs: function (usagePointId, purposeId) {
        var me = this,
            app = me.getApplication(),
            router = me.getController('Uni.controller.history.Router'),
            periodsStore = me.getStore('Imt.usagepointmanagement.store.Periods'),
            purposesStore = me.getStore('Imt.usagepointmanagement.store.Purposes'),
            mainView = Ext.ComponentQuery.query('#contentPanel')[0],
            extraParams = {
                usagePointId: usagePointId,
                purposeId: purposeId
            },
            dependenciesCounter = 4,
            defaultPeriod,
            usagePoint,
            purposes;

        mainView.setLoading();

        me.getStore('Imt.purpose.store.ValidationTasks').getProxy().extraParams = extraParams;
        me.getStore('Imt.purpose.store.EstimationTasks').getProxy().extraParams = extraParams;
        me.getStore('Imt.usagepointmanagement.store.UsagePointTypes').load(onDependenciesLoad);

        periodsStore.getProxy().extraParams = {usagePointId: usagePointId, purposeId: purposeId};
        periodsStore.load(function (records) {
            defaultPeriod = records[0].getId();
            onDependenciesLoad();
        });

        me.getModel('Imt.usagepointmanagement.model.UsagePoint').load(usagePointId, {
            success: function (record) {
                usagePoint = record;
                app.fireEvent('usagePointLoaded', usagePoint);
                onDependenciesLoad();
            }
        });

        purposesStore.getProxy().extraParams = {usagePointId: usagePointId};
        purposesStore.load(function (records) {
            purposes = records;
            app.fireEvent('purposes-loaded', purposes);
            onDependenciesLoad();
        });

        function onDependenciesLoad() {
            var purpose,
                widget;

            dependenciesCounter--;
            if (!dependenciesCounter) {
                usagePoint.set('purposes', purposes);
                purpose = _.find(purposes, function (p) {
                    return p.getId() == purposeId
                });
                widget = Ext.widget('purpose-outputs', {
                    itemId: 'purpose-outputs',
                    router: router,
                    usagePoint: usagePoint,
                    purposes: purposes,
                    purpose: purpose,
                    defaultPeriod: defaultPeriod
                });

                widget.down('#purpose-details-form').loadRecord(purpose);
                app.fireEvent('changecontentevent', widget);
                if (mainView.down('purpose-actions-menu')) {
                    mainView.down('purpose-actions-menu').record = purpose;
                }
                mainView.setLoading(false);
                me.loadOutputs(usagePointId, purposeId);
            }
        }
    },

    showOutputPreview: function (selectionModel, record) {
        var me = this;

        me.getOutputPreview().loadRecord(record);
    },

    makeLinkToOutputs: function (router) {
        var link = '<a href="{0}">' + Uni.I18n.translate('general.outputs', 'IMT', 'Outputs').toLowerCase() + '</a>';
        return Ext.String.format(link, router.getRoute('usagepoints/view/purpose').buildUrl());
    },

    showOutputDefaultTab: function(usagePointId, purposeId, outputId, tab) {
        var me = this,
            app = me.getApplication(),
            router = me.getController('Uni.controller.history.Router'),
            usagePointsController = me.getController('Imt.usagepointmanagement.controller.View'),
            mainView = Ext.ComponentQuery.query('#contentPanel')[0],
            prevNextListLink = me.makeLinkToOutputs(router),
            outputModel,
            dependenciesCounter,
            displayPage,
            usagePoint,
            purposes,
            purpose,
            outputs,
            output,
            validationConfigurationStore = me.getStore('Imt.purpose.store.OutputValidationConfiguration'),
            estimationConfigurationStore = me.getStore('Imt.purpose.store.OutputEstimationConfiguration');

        if (!tab) {
            window.location.replace(router.getRoute('usagepoints/view/purpose/output').buildUrl({tab: 'readings'}));
        } else {
            validationConfigurationStore.getProxy().extraParams = {usagePointId: usagePointId, purposeId: purposeId, outputId: outputId};
            validationConfigurationStore.load(function () {
                displayPage();
            });

            estimationConfigurationStore.getProxy().extraParams = {usagePointId: usagePointId, purposeId: purposeId, outputId: outputId};
            estimationConfigurationStore.load(function () {
                displayPage();
            });

            outputModel = me.getModel('Imt.purpose.model.Output');
            dependenciesCounter = 5;
            displayPage = function () {
                var widget;

                dependenciesCounter--;
                if (!dependenciesCounter) {
                    if ((tab === 'validation' && !validationConfigurationStore.getCount()) || (tab === 'estimation' && output.get('outputType') === 'channel' && !estimationConfigurationStore.getCount())) {
                        window.location.replace(router.getRoute('usagepoints/view/purpose/output').buildUrl({tab: 'readings'}));
                        return;
                    }
                    mainView.setLoading(false);
                    app.fireEvent('outputs-loaded', outputs);
                    app.fireEvent('output-loaded', output);
                    widget = Ext.widget('output-channel-main', {
                        itemId: 'output-channel-main',
                        router: router,
                        usagePoint: usagePoint,
                        purposes: purposes,
                        purpose: purpose,
                        outputs: outputs,
                        output: output,
                        interval: me.getInterval(output),
                        prevNextListLink: prevNextListLink,
                        controller: me,
                        validationConfigurationStore: validationConfigurationStore,
                        estimationConfigurationStore: estimationConfigurationStore,
                        tab: tab
                    });
                    app.fireEvent('changecontentevent', widget);
                    widget.down('output-specifications-form').loadRecord(output);
                }
            };

            mainView.setLoading();

            usagePointsController.loadUsagePoint(usagePointId, {
                success: function (types, up, records) {
                    usagePoint = up;
                    purposes = records;
                    purpose = _.find(records, function (p) {
                        return p.getId() == purposeId
                    });
                    displayPage();
                }
            });

            // avoid handling events of the outputs store by components on the purpose page
            var outputsStore = me.getStore('Imt.purpose.store.Outputs');
            outputsStore.suspendEvents();
            me.loadOutputs(usagePointId, purposeId, function (records) {
                outputsStore.resumeEvents();
                outputs = records;
                displayPage();
            });
            outputModel.getProxy().extraParams = {usagePointId: usagePointId, purposeId: purposeId};
            outputModel.load(outputId, {
                success: function (record) {
                    output = record;
                    if(record.get('outputType') === "channel"){
                        var estimationRulesStore = me.getStore('Imt.purpose.store.EstimationRules');
                        estimationRulesStore.getProxy().extraParams = {usagePointId: usagePointId, purposeId: purposeId, outputId: outputId}
                        estimationRulesStore.load(function(records){
                            me.hasEstimationRule = Boolean(records.length);
                            displayPage();
                        });
                    } else {
                        displayPage();
                    }
                }
            });
        }
    },

    getInterval: function(output) {
        var me = this,
            intervalStore = me.getStore('Uni.store.DataIntervalAndZoomLevels');

        return intervalStore.getIntervalRecord(output.get('interval'));
    },

    showSpecificationsTab: function(panel) {
        var me = this,
            router = me.getController('Uni.controller.history.Router');

        if (router.arguments.tab != 'specifications') {
            Uni.util.History.suspendEventsForNextCall();
            Uni.util.History.setParsePath(false);
            router.arguments.tab = 'specifications';
            router.getRoute('usagepoints/view/purpose/output').forward();
        }
    },

    showValidationTab: function() {
        var me = this,
            router = me.getController('Uni.controller.history.Router');

        if (router.arguments.tab != 'validation') {
            Uni.util.History.suspendEventsForNextCall();
            Uni.util.History.setParsePath(false);
            router.arguments.tab = 'validation';
            router.getRoute('usagepoints/view/purpose/output').forward();
        }
    },

    showEstimationTab: function() {
        var me = this,
            router = me.getController('Uni.controller.history.Router');

        if (router.arguments.tab != 'estimation') {
            Uni.util.History.suspendEventsForNextCall();
            Uni.util.History.setParsePath(false);
            router.arguments.tab = 'estimation';
            router.getRoute('usagepoints/view/purpose/output').forward();
        }
    },

    showReadingsTab: function(panel) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            output = panel.output,
            readingsStore;
        switch (output.get('outputType')) {
            case 'channel':
                readingsStore = me.getStore('Imt.purpose.store.Readings');
                break;
            case 'register':
                readingsStore = me.getStore('Imt.purpose.store.RegisterReadings');
                break;
        }

        if (router.arguments.tab != 'readings') {
            Uni.util.History.suspendEventsForNextCall();
            Uni.util.History.setParsePath(false);
            router.arguments.tab = 'readings';
            router.getRoute('usagepoints/view/purpose/output').forward();
        }

        readingsStore.getProxy().extraParams = {
            usagePointId: panel.usagePoint.get('name'),
            purposeId: panel.purpose.getId(),
            outputId: panel.output.getId()
        };

        readingsStore.load();
    },

    onGraphResize: function (graphView, width, height) {
        if (graphView.chart) {
            graphView.chart.setSize(width, height, false);
        }
    },

    chooseAction: function (menu, item) {
        var me = this;

        if (item) {
            switch (item.action) {
                case 'validateNow':
                    me.validatePurpose(menu.record);
                    break;
            }
            switch (item.action) {
                case 'estimateNow':
                    me.estimatePurpose(menu.record);
                    break;
            }
        }
    },

    validatePurpose: function (purpose) {
        var me = this,
            usagePoint = Ext.ComponentQuery.query('#contentPanel')[0].down('purpose-outputs').usagePoint,
            lastChecked = purpose.get('validationInfo').lastChecked,
            confirmationWindow = Ext.create('Uni.view.window.Confirmation', {
                itemId: 'purpose-validateNowConfirmationWindow',
                confirmText: Uni.I18n.translate('general.validate', 'IMT', 'Validate'),
                closeAction: 'destroy',
                confirmation: _.once(Ext.bind(me.onValidateNow, me, [purpose, usagePoint, getConfirmationWindow]))
            });

        confirmationWindow.insert(1, {
            xtype: 'validation-date',
            itemId: 'purpose-rdg-validation-run',
            defaultDate: lastChecked ? new Date(lastChecked) : new Date(),
            padding: '-10 0 0 45'
        });
        confirmationWindow.show({
            title: Uni.I18n.translate('purpose.validateNow', 'IMT', "Validate data for '{0}' purpose on usage point '{1}'?",
                [purpose.get('name'), usagePoint.get('name')], false),
            icon: 'icon-question4'
        });

        function getConfirmationWindow() {
            return confirmationWindow
        }
    },

    onValidateNow: function (purpose, usagePoint, getConfirmationWindow) {
        var me = this,
            confWindow = getConfirmationWindow(),
            progressbar = confWindow.insert(2, {
                xtype: 'progressbar',
                itemId: 'validation-progressbar',
                margin: '5 0 15 0'
            }),
            lastChecked;

        progressbar.wait({
            duration: 60000,
            fn: Ext.bind(me.onTooLongOperation, me, [purpose, confWindow, {
                title: Uni.I18n.translate('purpose.dataValidation.timeout.title1', 'IMT', 'Data validation takes longer than expected'),
                msg: Uni.I18n.translate('purpose.dataValidation.timeout.message', 'IMT', 'Data validation takes longer than expected and will continue in the background.')
            }])
        });

        if (confWindow.down('#purpose-rdg-validation-run').getValue().validation === 'newValidation') {
            lastChecked = confWindow.down('#purpose-dtm-validation-from-date').getValue().getTime();
        } else if (purpose.get('validationInfo').lastChecked) {
            lastChecked = purpose.get('validationInfo').lastChecked;
        } else {
            lastChecked = new Date().getTime();
        }

        purpose.set('validationInfo', {lastChecked: lastChecked});
        me.doOperation(purpose, usagePoint, confWindow, Uni.I18n.translate('purpose.successMsg', 'IMT', 'Data validation for the purpose is completed'), 'validate');
    },

    estimatePurpose: function (purpose) {
        var me = this,
            usagePoint = Ext.ComponentQuery.query('#contentPanel')[0].down('purpose-outputs').usagePoint,
            confirmationWindow = Ext.widget('confirmation-window', {
                itemId: 'estimate-now-confirmation-window',
                closeAction: 'destroy',
                confirmText: Uni.I18n.translate('general.estimate', 'IMT', 'Estimate'),
                confirmation: _.once(Ext.bind(me.onEstimateNow, me, [purpose, usagePoint, getConfirmationWindow]))
            });

        confirmationWindow.show({
            title: Uni.I18n.translate('purpose.estimateNow', 'IMT', "Estimate data for '{0}' purpose on usage point '{1}'?",
                [purpose.get('name'), usagePoint.get('name')], false),
            icon: 'icon-question4'
        });

        function getConfirmationWindow() {
            return confirmationWindow
        }
    },

    onEstimateNow: function (purpose, usagePoint, getConfirmationWindow) {
        var me = this,
            confirmationWindow = getConfirmationWindow(),
            progressbar = confirmationWindow.insert(1, {
                xtype: 'progressbar',
                itemId: 'estimation-progressbar',
                margin: '5 0 15 0'
            });

        progressbar.wait({
            duration: 60000,
            fn: Ext.bind(me.onTooLongOperation, me, [purpose, confirmationWindow, {
                title: Uni.I18n.translate('purpose.dataEstimation.timeout.title1', 'IMT', 'Data estimation takes longer than expected'),
                msg: Uni.I18n.translate('purpose.dataEstimation.timeout.message', 'IMT', 'Data estimation takes longer than expected and will continue in the background.')
            }])
        });

        me.doOperation(purpose, usagePoint, confirmationWindow, Uni.I18n.translate('purpose.dataEstimation.successMsg', 'IMT', 'Data estimation for the purpose is completed'), 'estimate');
    },

    onTooLongOperation: function (purpose, confirmationWindow, errorMessageConfig) {
        var errorMessage = Ext.widget('messagebox', {
            closeAction: 'destroy',
            buttons: [
                {
                    text: Uni.I18n.translate('general.close', 'IMT', 'Close'),
                    ui: 'remove',
                    handler: function () {
                        this.up('window').close();
                    }
                }
            ]
        });

        Ext.suspendLayouts();
        confirmationWindow.close();
        errorMessage.show(Ext.apply({
            ui: 'notification-error',
            modal: false,
            icon: Ext.MessageBox.ERROR
        }, errorMessageConfig));
        Ext.resumeLayouts(true);
    },

    doOperation: function (purpose, usagePoint, confirmationWindow, successMessage, action) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            purposePage = me.getPurposePage();

        purpose[action](usagePoint, {
            isNotEdit: true,
            notHandleTimeout: true,
            callback: function (options, success, response) {
                if (response.status) {
                    confirmationWindow.close();
                }
                if (purposePage.rendered && success) {
                    me.getApplication().fireEvent('acknowledge', successMessage);
                    router.getRoute().forward(null, Ext.Object.fromQueryString(router.getQueryString()));
                }
            }
        });
    },

    showEditValidationRuleWithAttributes: function() {
        this.showEditRuleWithAttributes('validation');
    },

    showEditEstimationRuleWithAttributes: function() {
        this.showEditRuleWithAttributes('estimation');
    },

    showEditRuleWithAttributes: function(type) {
        var me = this,
            app = me.getApplication(),
            router = me.getController('Uni.controller.history.Router'),
            usagePointId = router.arguments.usagePointId,
            purposeId = router.arguments.purposeId,
            outputId = router.arguments.outputId,
            mainView = Ext.ComponentQuery.query('#contentPanel')[0],
            usagePointsController = me.getController('Imt.usagepointmanagement.controller.View'),
            dependenciesCounter = 3,
            outputModel = me.getModel('Imt.purpose.model.Output'),
            ruleWithAttributesModel = type === 'validation' ? me.getModel('Imt.purpose.model.OutputValidationConfiguration') : me.getModel('Imt.purpose.model.OutputEstimationConfiguration'),
            form,
            output,
            rule,
            widget;

        mainView.setLoading();
        outputModel.getProxy().extraParams = {usagePointId: usagePointId, purposeId: purposeId};
        ruleWithAttributesModel.getProxy().extraParams = {usagePointId: usagePointId, purposeId: purposeId, outputId: outputId};

        usagePointsController.loadUsagePoint(usagePointId, {
            success: function () {
                displayPage();
            }
        });
        outputModel.load(outputId, {
            success: function (record) {
                output = record;
                displayPage();
            }
        });
        ruleWithAttributesModel.load(router.arguments.ruleId, {
            success: function (record) {
                rule = record;
                displayPage();
            }
        });

        function displayPage() {
            dependenciesCounter--;
            if (!dependenciesCounter) {
                mainView.setLoading(false);
                app.fireEvent('output-loaded', output);
                widget = Ext.widget('rule-with-attributes-edit', {
                    itemId: 'rule-with-attributes-edit-' + type,
                    type: type,
                    route: router.getRoute('usagepoints/view/purpose/output'),
                    application: me.getApplication()
                });
                form = widget.down('#rule-with-attributes-edit-form');
                form.loadRecord(rule);
                form.setTitle(Ext.String.format("{0} '{1}'", Uni.I18n.translate('general.editAttributesFor', 'IMT', 'Edit attributes for'), rule.get('name')));
                form.down('property-form').loadRecord(rule);
                app.fireEvent('rule-with-attributes-loaded', rule);
                app.fireEvent('changecontentevent', widget);
            }
        }
    }
});