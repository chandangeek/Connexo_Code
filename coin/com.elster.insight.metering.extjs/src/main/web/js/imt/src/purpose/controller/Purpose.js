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
        'Imt.purpose.store.RegisterReadings'
    ],

    models: [
        'Imt.purpose.model.Output',
        'Imt.purpose.model.Reading',
        'Imt.usagepointmanagement.model.Purpose'
    ],

    views: [
        'Imt.purpose.view.Outputs',
        'Imt.purpose.view.OutputChannelMain'
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

    loadOutputs: function (mRID, purposeId, callback) {
        var me = this,
            outputsStore = me.getStore('Imt.purpose.store.Outputs');

        outputsStore.getProxy().extraParams = {mRID: mRID, purposeId: purposeId};
        outputsStore.load(callback);
    },

    showOutputs: function (mRID, purposeId) {
        var me = this,
            app = me.getApplication(),
            router = me.getController('Uni.controller.history.Router'),
            usagePointsController = me.getController('Imt.usagepointmanagement.controller.View'),
            mainView = Ext.ComponentQuery.query('#contentPanel')[0];


        mainView.setLoading();
        usagePointsController.loadUsagePoint(mRID, {
            success: function (types, usagePoint, purposes) {
                var purpose = _.find(purposes, function(p){return p.getId() == purposeId});
                app.fireEvent('changecontentevent', Ext.widget('purpose-outputs', {
                    itemId: 'purpose-outputs',
                    router: router,
                    usagePoint: usagePoint,
                    purposes: purposes,
                    purpose: purpose
                }));
                if (mainView.down('purpose-actions-menu')) {
                    mainView.down('purpose-actions-menu').record = purpose;
                }                
                mainView.setLoading(false);
                me.loadOutputs(mRID, purposeId);
            },
            failure: function () {
                mainView.setLoading(false);
            }
        });
    },

    makeLinkToOutputs: function (router) {
        var link = '<a href="{0}">' + Uni.I18n.translate('general.outputs', 'IMT', 'Outputs').toLowerCase() + '</a>';
        return Ext.String.format(link, router.getRoute('usagepoints/view/purpose').buildUrl());
    },

    showOutputDefaultTab: function(mRID, purposeId, outputId, tab) {
        var me = this,
            app = me.getApplication(),
            router = me.getController('Uni.controller.history.Router'),
            usagePointsController = me.getController('Imt.usagepointmanagement.controller.View'),
            mainView = Ext.ComponentQuery.query('#contentPanel')[0],
            prevNextListLink = me.makeLinkToOutputs(router);

        if (!tab) {
            window.location.replace(router.getRoute('usagepoints/view/purpose/output').buildUrl({tab: 'readings'}));
        } else {
            mainView.setLoading();
            usagePointsController.loadUsagePoint(mRID, {
                success: function (types, usagePoint, purposes) {
                    me.loadOutputs(mRID, purposeId, function (outputs) {
                        app.fireEvent('outputs-loaded', outputs);
                        var output = _.find(outputs, function (o) {
                            return o.getId() == outputId
                        });

                        //me.getModel('Imt.purpose.model.Output').load(outputId, {
                        //    success: function (output) {
                        var purpose = _.find(purposes, function (p) {
                            return p.getId() == purposeId
                        });
                        app.fireEvent('output-loaded', output);
                        var widget = Ext.widget('output-channel-main', {
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
                        mainView.setLoading(false);
                        widget.down('output-specifications-form').loadRecord(output);

                        //},
                        //failure: function () {
                        //    mainView.setLoading(false);
                        //}
                        //});
                    });
                },
                failure: function () {
                    mainView.setLoading(false);
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
            mRID: panel.usagePoint.get('mRID'),
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
                [purpose.get('name'), Ext.ComponentQuery.query('#contentPanel')[0].down('purpose-outputs').usagePoint.get('mRID')], false),
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
                    itemId: 'purpose-pnl-validation-date-errors',
                    hidden: true,
                    bodyStyle: {
                        color: '#eb5642',
                        padding: '0 0 15px 65px'
                    },
                    html: ''
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
            lastChecked;

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
            mRID: Ext.ComponentQuery.query('#contentPanel')[0].down('purpose-outputs').usagePoint.get('mRID')
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