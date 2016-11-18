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
        'Imt.purpose.store.ValidationTasks'
    ],

    models: [
        'Imt.purpose.model.Output',
        'Imt.purpose.model.Reading',
        'Imt.usagepointmanagement.model.ValidationInfo',
        'Imt.usagepointmanagement.model.SuspectReason',
        'Imt.usagepointmanagement.model.Purpose',
        'Imt.usagepointmanagement.model.UsagePoint'
    ],

    views: [
        'Imt.purpose.view.Outputs',
        'Imt.purpose.view.OutputChannelMain',
        'Imt.purpose.view.ValidationStatusForm'
    ],

    refs: [
        {
            ref: 'readingPreviewPanel',
            selector: 'output-channel-main reading-preview'
        }
    ],

    init: function () {
        this.control({
            'output-channel-main readings-list': {
                select: function (selectionModel, record) {
                    this.getReadingPreviewPanel().updateForm(record);
                }
            },
            'output-channel-main #readings-graph': {
                resize: this.onGraphResize
            },
            'purpose-outputs purpose-actions-menu': {
                click: this.chooseAction
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
            purposesStore = me.getStore('Imt.usagepointmanagement.store.Purposes'),
            mainView = Ext.ComponentQuery.query('#contentPanel')[0];

        mainView.setLoading();
        
        me.getStore('Imt.usagepointmanagement.store.UsagePointTypes').load(function(usagePointTypes, op, success) {
            if (success) {
                me.getModel('Imt.usagepointmanagement.model.UsagePoint').load(usagePointId, {
                    success: function (usagePoint) {
                        app.fireEvent('usagePointLoaded', usagePoint);
                        purposesStore.getProxy().extraParams = {
                            usagePointId: usagePointId
                        };
                        purposesStore.load(function(purposes) {
                            usagePoint.set('purposes', purposes);
                            app.fireEvent('purposes-loaded', purposes);
                            var purpose = _.find(purposes, function (p) {
                                    return p.getId() == purposeId
                                }),
                                widget = Ext.widget('purpose-outputs', {
                                    itemId: 'purpose-outputs',
                                    router: router,
                                    usagePoint: usagePoint,
                                    purposes: purposes,
                                    purpose: purpose
                                });
                            widget.down('#purpose-details-form').loadRecord(purpose);
                            app.fireEvent('changecontentevent', widget);
                            if (mainView.down('purpose-actions-menu')) {
                                mainView.down('purpose-actions-menu').record = purpose;
                            }
                            mainView.setLoading(false);
                            me.loadOutputs(usagePointId, purposeId);
                        });
                    }                    
                });
            } else {
                mainView.setLoading(false);
            }
        });
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
            output;

        if (!tab) {
            window.location.replace(router.getRoute('usagepoints/view/purpose/output').buildUrl({tab: 'readings'}));
        } else {
            outputModel = me.getModel('Imt.purpose.model.Output');
            dependenciesCounter = 3;
            displayPage = function () {
                var widget;

                dependenciesCounter--;
                if (!dependenciesCounter) {
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
            me.loadOutputs(usagePointId, purposeId, function (records) {
                outputs = records;
                displayPage();
            });
            outputModel.getProxy().extraParams = {usagePointId: usagePointId, purposeId: purposeId};
            outputModel.load(outputId, {
                success: function (record) {
                    output = record;
                    displayPage();
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
        }
    },

    validatePurpose: function (purpose) {
        var me = this,
            confirmationWindow = Ext.create('Uni.view.window.Confirmation', {
                itemId: 'purpose-validateNowConfirmationWindow',
                confirmText: Uni.I18n.translate('general.validate', 'IMT', 'Validate'),
                confirmation: function () {
                    me.onValidateNow(this, purpose);
                }
            });

        confirmationWindow.insert(1, me.getActivationConfirmationContent(purpose));
        confirmationWindow.show({
            title: Uni.I18n.translate('purpose.validateNow', 'IMT', 'Validate data for "{0}" purpose on usage point "{1}"',
                [purpose.get('name'), Ext.ComponentQuery.query('#contentPanel')[0].down('purpose-outputs').usagePoint.get('name')], false),
            icon: 'icon-question4',
            msg: ''
        });
        confirmationWindow.on('close', function () {
            this.destroy();
        });
    },

    getActivationConfirmationContent: function (purpose) {
        var lastCheckedDate = purpose.get('validationInfo').lastChecked ? Uni.DateTime.formatDateShort(new Date(purpose.get('validationInfo').lastChecked)) :  Uni.DateTime.formatDateShort(new Date());
        return Ext.create('Ext.container.Container', {
            defaults: {
                labelAlign: 'left'
            },
            items: [
                {
                    xtype: 'radiogroup',
                    itemId: 'purpose-rdg-validation-run',
                    columns: 1,
                    padding: '-10 0 0 45',
                    defaults: {
                        name: 'validationRun'
                    },
                    items: [
                        {
                            boxLabel: Uni.I18n.translate('validationResults.validate.fromLast', 'IMT', 'Validate data from last validation ({0})',
                                lastCheckedDate),
                            inputValue: 'lastValidation',
                            itemId: 'purpose-rdo-validate-from-last',
                            xtype: 'radiofield',
                            checked: true,
                            name: 'validation'
                        },
                        {
                            xtype: 'container',
                            layout: {
                                type: 'hbox',
                                align: 'stretch'
                            },
                            width: 300,
                            items: [
                                {
                                    boxLabel: Uni.I18n.translate('validationResults.validate.from', 'IMT', 'Validate data from'),
                                    inputValue: 'newValidation',
                                    itemId: 'purpose-rdo-validate-from-date',
                                    xtype: 'radiofield',
                                    name: 'validation'
                                },
                                {
                                    xtype: 'datefield',
                                    itemId: 'purpose-dtm-validation-from-date',
                                    editable: false,
                                    showToday: false,
                                    value: purpose.get('validationInfo').lastChecked ? new Date(purpose.get('validationInfo').lastChecked) : new Date(),
                                    fieldLabel: '  ',
                                    labelWidth: 10,
                                    width: 150,
                                    listeners: {
                                        focus: {
                                            fn: function () {
                                                var radioButton = Ext.ComponentQuery.query('#purpose-rdg-validation-run #purpose-rdo-validate-from-date')[0];
                                                radioButton.setValue(true);
                                            }
                                        }
                                    }
                                }
                            ]
                        }
                    ]
                },
                {
                    xtype: 'panel',
                    itemId: 'purpose-pnl-validation-progress',
                    layout: 'fit',
                    padding: '0 0 0 50'
                }
            ]
        });
    },

    onValidateNow: function (confWindow, purpose) {
        var me = this,
            lastChecked,
            usagePoint = Ext.ComponentQuery.query('#contentPanel')[0].down('purpose-outputs').usagePoint;

        if (confWindow.down('#purpose-rdg-validation-run').getValue().validation === 'newValidation') {
            lastChecked = confWindow.down('#purpose-dtm-validation-from-date').getValue().getTime();
        } else if (purpose.get('validationInfo').lastChecked) {
            lastChecked = purpose.get('validationInfo').lastChecked;
        } else {
            lastChecked = new Date().getTime();
        }
        confWindow.down('#purpose-pnl-validation-progress').add(Ext.create('Ext.ProgressBar', {
            margin: '5 0 15 0'
        })).wait({
            duration: 120000,
            text: Uni.I18n.translate('purpose.dataValidation.isInProgress', 'IMT', 'Data validation is in progress. Please wait...'),
            fn: function () {
                confWindow.destroy();
                Ext.widget('messagebox', {
                    buttons: [
                        {
                            text: Uni.I18n.translate('general.close', 'IMT', 'Close'),
                            ui: 'remove',
                            handler: function () {
                                this.up('window').close();
                            }
                        }
                    ],
                    listeners: {
                        close: function () {
                            this.destroy();
                        }
                    }
                }).show({
                    ui: 'notification-error',
                    title: Uni.I18n.translate('purpose.dataValidation.timeout.title', 'IMT', 'Data validation takes longer as expected'),
                    msg: Uni.I18n.translate('purpose.dataValidation.timeout.msg', 'IMT', 'Data validation takes longer as expected. Data validation will continue in the background'),
                    icon: Ext.MessageBox.ERROR
                });
            }
        });

        purpose.set('validationInfo', {lastChecked: lastChecked});
        purpose.getProxy().extraParams = {
            usagePointId: usagePoint.get('name'),
            upVersion: usagePoint.get('version')
        };
        purpose.save({
            isNotEdit: true,
            callback: function (model, operation, success) {
                confWindow.destroy();
                if (success) {
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('purpose.successMsg', 'IMT', 'Data validation for the purpose is completed'));
                    me.getController('Uni.controller.history.Router').getRoute().forward();
                }
            }
        });
    }
});