/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Pkj.controller.BulkAction', {
    extend: 'Ext.app.Controller',

    views: [
        'Pkj.view.bulk.Browse',
        'Pkj.view.bulk.Wizard'
    ],

    requires: [],

    stores: [
        'Pkj.store.CertificatesBulk',
        'Pkj.store.Certificates',
        'Pkj.store.Timeouts'

    ],

    refs: [
        {
            ref: 'navigationMenu',
            selector: '#certificates-bulk-navigation'
        },
        {
            ref: 'wizard',
            selector: '#certificates-wizard'
        },
        {
            ref: 'readingTypesGrid',
            selector: '#certificates-selection-grid'
        },
        {
            ref: 'confirmPage',
            selector: '#reading-types-bulk-step4'
        },
        {
            ref: 'statusPage',
            selector: '#reading-types-bulk-step5'
        }
    ],

    operation: 'activate',

    init: function () {
        this.control({
            'certificates-wizard #reading-types-bulk-next': {
                click: this.nextClick
            },
            'certificates-wizard #reading-types-bulk-confirm': {
                click: this.nextClick
            },
            'certificates-wizard #reading-types-bulk-back': {
                click: this.backClick
            },
            'certificates-wizard #reading-types-bulk-cancel': {
                click: this.goBack
            },
            'certificates-wizard #reading-types-bulk-finish': {
                click: this.goBack
            }
        });
    },

    showOverview: function () {
        var me = this,
            certificatesStore = Ext.getStore('Pkj.store.Certificates'),
            router = me.getController('Uni.controller.history.Router'),
            readingTypesStoreBulk = Ext.getStore('Pkj.store.CertificatesBulk'),
            viewport = Ext.ComponentQuery.query('viewport')[0],
            filterPanel = viewport.down('uni-grid-filterpaneltop'),
            filter,
            widget;

        viewport.setLoading(true);
        if (certificatesStore.getCount()) {
            filter = filterPanel.createFiltersObject(filterPanel.getFilterParams());
            readingTypesStoreBulk.load({
                params: {
                    filter: filter
                },
                callback: function () {
                    widget = Ext.widget('certificates-bulk-browse');
                    me.getApplication().fireEvent('changecontentevent', widget);
                    viewport.setLoading(false);
                }
            });
        } else {
            me.goBack();
            viewport.setLoading(false);
        }

    },

    nextClick: function () {
        var me = this,
            layout = this.getWizard().getLayout(),
            readingTypesStoreBulk = Ext.getStore('Pkj.store.CertificatesBulk'),
            currentCmp = layout.getActiveItem();
        Ext.suspendLayouts();

        switch (currentCmp.name) {
            case 'selectReadingTypes':
                me.AllreadingTypes = me.getReadingTypesGrid().isAllSelected();
                if (!me.AllreadingTypes) {
                    me.readingTypes = me.getReadingTypesGrid().getSelectionModel().getSelection();
                    if (me.readingTypes.length) {
                        currentCmp.down('#step-selection-error').hide();
                        currentCmp.down('#step1-errors').hide();
                        me.goNextStep();
                    } else {
                        currentCmp.down('#step-selection-error').show();
                        currentCmp.down('#step1-errors').show();
                    }
                } else {
                    var arr = [];
                    readingTypesStoreBulk.each(function(record){arr.push(record)});
                    me.readingTypes = arr;
                    currentCmp.down('#step-selection-error').hide();
                    currentCmp.down('#step1-errors').hide();
                    me.goNextStep();
                }
                break;
            case 'selectOperation':
                me.operation = currentCmp.down('#reading-types-bulk-step2-radio-group').getValue().operation;
                me.goNextStep();
                break;
            case 'selectActionItems':
                me.timeout = currentCmp.down('#timeout').getValue();
                if(currentCmp.down('form').isValid()){
                    currentCmp.down('#step3-errors').hide();
                    me.timeout = currentCmp.down('#timeout').getValue();
                    var jsonData = {
                        timeout: me.timeout,
                        bulk: {
                            certificatesIds: _.map(me.readingTypes, function(record){return record.get('id')})
                        }
                    };


                    Ext.Ajax.request({
                        url: '/api/pir/certificates/checkBulkRevoke',
                        method: 'POST',
                        jsonData: jsonData,
                        timeout: me.timeout,
                        success: function (response) {
                            var decoded = response.responseText ? Ext.decode(response.responseText, true) : null;
                            me.validationResult = decoded;
                            var title = Uni.I18n.translate('certificates.bulk.step4applyRevoke', 'PKJ', 'Revoke {0} out of {1} certificates?', [decoded.bulk.valid, decoded.bulk.total]);
                            var message = Uni.I18n.translate('certificates.bulk.step4RevokeMsgx', 'PKJ', 'You will not be able to use these certificates in the system.');

                            me.getConfirmPage().addNotificationPanel(title, message);
                            if(!decoded.isOnline){
                                var title = Uni.I18n.translate('certificates.bulk.step4applyRevokeCAIsOffline', 'PKJ', 'CA is offline');
                                var message = Uni.I18n.translate(
                                    'certificates.bulk.step4RevokeErrorMsg',
                                    'PKJ',
                                    'The certificates will be marked as revoked in the system ' +
                                    '<br>' +
                                    'but you need to manually send them to the Certificate Authority that will change their revocation status');

                                me.getConfirmPage().addErrorPanel(title, message, undefined,'errorPanelCAOff')
                            }
                            if (decoded.bulk.withUsages && decoded.bulk.withUsages.length > 0) {
                                var title = Uni.I18n.translate('certificates.bulk.step4UnableRevoke', 'PKJ', 'Unable to revoke {0} certificates', decoded.bulk.withUsages.length);
                                var message = Uni.I18n.translate('certificates.bulk.step4UnableRevokeUsagesMsgx', 'PKJ', 'These certificates are used on a device, security accessor or user directory:');
                                me.getConfirmPage().router = me.getController('Uni.controller.history.Router');
                                me.getConfirmPage().addErrorPanel(title, message, decoded.bulk.withUsages, 'errorPanelInUsed')
                            }
                            if (decoded.bulk.withWrongStatus && decoded.bulk.withWrongStatus.length > 0) {
                                var title = Uni.I18n.translate('certificates.bulk.step4UnableRevoke', 'PKJ', 'Unable to revoke {0} certificates', decoded.bulk.withWrongStatus.length);
                                var message = Uni.I18n.translate('certificates.bulk.step4UnableRevokeStatusMsgx', 'PKJ', "These certificates are in 'Requested' or 'Revoked' status:");
                                me.getConfirmPage().router = me.getController('Uni.controller.history.Router');
                                me.getConfirmPage().addErrorPanel(title, message, decoded.bulk.withWrongStatus, 'errorPanelInWrongStatus')
                            }
                            me.goNextStep();
                        },
                        failure: function (response, request) {
                        }
                    });
                } else {
                    currentCmp.down('#step3-errors').show()
                }

                break;
            case 'confirmPage':
                currentCmp.add(
                    {
                        xtype: 'progressbar',
                        itemId: 'progressbar',
                        text: Uni.I18n.translate(
                            'certificates.bulk.step4RevokeprogressbarMsg',
                            'PKJ', 'Revoking {0} certificates. Please wait...', me.validationResult.bulk.valid),
                        width: '50%'
                    });
                var progressbar = currentCmp.down('progressbar');
                progressbar.wait({
                    interval: 50,
                    increment: 20
                });
                me.getWizard().down('#reading-types-bulk-confirm').disable()
                var jsonDataBulk = {
                    timeout: me.timeout,
                    bulk: {
                        certificatesIds: me.validationResult ? me.validationResult.bulk.certificatesIds : []
                    }
                };
                var withErrorsWidget, withUsagesWidget, withWrongStatusWidget, router = me.getController('Uni.controller.history.Router');
                Ext.Ajax.request({
                    url: '/api/pir/certificates/bulkRevoke',
                    method: 'POST',
                    jsonData: jsonDataBulk,
                    timeout: me.timeout,
                    success: function (response) {
                        var decoded = response.responseText ? Ext.decode(response.responseText, true) : null;
                        var title = Uni.I18n.translate('certificates.bulk.step5applyRevoke', 'PKJ', 'Revoke certificates');
                        var message = Uni.I18n.translate('certificates.bulk.step5RevokeMsgxdds',
                            'PKJ',
                            '{0} out of {1} certificates successfully revoked.', [decoded.revokedCount, decoded.totalCount]);
                        if (decoded.withErrors.length > 0) {
                            withErrorsWidget = Ext.create('Ext.container.Container', {
                                items: [{
                                    xtype: 'component',
                                    html: '<h3 style="font-weight: 600; color: #EB5642">' +
                                    Uni.I18n.translate('certificates.bulk.step5applyRevokeCA', 'PKJ', 'These certificates couldn\'t be revoked by the CA:')
                                    + '</h3>'

                                }]
                            });
                            _.map(_.first(decoded.withErrors, 10), function (cert) {
                                var url = router.getRoute('administration/certificates/view').buildUrl({certificateId: cert.id});
                                withErrorsWidget.add({
                                    xtype: 'component',
                                    html: '<a href="' + url + '">' + Ext.String.htmlEncode(cert.name) + '</a>'
                                });
                            });
                        }
                        if (decoded.withUsages.length > 0) {
                            withUsagesWidget = Ext.create('Ext.container.Container', {
                                items: [{
                                    xtype: 'component',
                                    html: '<h3 style="font-weight: 600; color: #EB5642">' +
                                    Uni.I18n.translate('certificates.bulk.step5applyRevokeCAerrors', 'PKJ', 'Unable to revoke {0} certificates', decoded.withUsagesCount)
                                    + '</h3>'

                                }, {
                                    xtype: 'component',
                                    html: Uni.I18n.translate('certificates.bulk.step5applyRevokeCAusages', 'PKJ', 'These certificates are used on a device, security accessor or user directory:')

                                }]
                            });
                            _.map(_.first(decoded.withUsages, 10), function (cert) {
                                var url = router.getRoute('administration/certificates/view').buildUrl({certificateId: cert.id});
                                withUsagesWidget.add({
                                    xtype: 'component',
                                    html: '<a href="' + url + '">' + Ext.String.htmlEncode(cert.name) + '</a>'
                                });
                            });
                        }
                        if (decoded.withWrongStatus.length > 0) {
                            withWrongStatusWidget = Ext.create('Ext.container.Container', {
                                items: [{
                                    xtype: 'component',
                                    html: '<h3 style="font-weight: 600; color: #EB5642">' +
                                    Uni.I18n.translate('certificates.bulk.step5applyRevokeCAerrors', 'PKJ', 'Unable to revoke {0} certificates', decoded.withWrongStatusCount)
                                    + '</h3>'

                                }, {
                                    xtype: 'component',
                                    html: Uni.I18n.translate('certificates.bulk.step5applyRevokeCAwrongStatus', 'PKJ', "These certificates are in 'Requested' or 'Revoked' status:")

                                }]
                            });
                            _.map(_.first(decoded.withWrongStatus, 10), function (cert) {
                                var url = router.getRoute('administration/certificates/view').buildUrl({certificateId: cert.id});
                                withWrongStatusWidget.add({
                                    xtype: 'component',
                                    html: '<a href="' + url + '">' + Ext.String.htmlEncode(cert.name) + '</a>'
                                });
                            });
                        }
                        me.getStatusPage().addNotificationPanel(title, message, [withUsagesWidget, withWrongStatusWidget, withErrorsWidget]);

                        me.goNextStep();
                    },
                    failure: function (response, request) {
                    }
                });
                break;
        }

        Ext.resumeLayouts(true);
    },

    goNextStep: function () {
        var me = this,
            layout = this.getWizard().getLayout(),
            nextCmp = layout.getNext();

        this.getNavigationMenu().moveNextStep();
        layout.setActiveItem(nextCmp);
        me.updateButtonsState(nextCmp);


    },

    backClick: function (btn, e, eOpts) {
        var me = this,
            layout = this.getWizard().getLayout(),
            currentCmp = layout.getActiveItem(),
            prevCmp = layout.getPrev();

        Ext.suspendLayouts();
        this.getNavigationMenu().movePrevStep();
        layout.setActiveItem(prevCmp);
        me.updateButtonsState(prevCmp);
        Ext.resumeLayouts(true);
    },

    updateButtonsState: function (nextCmp) {
        var me = this,
            wizard = me.getWizard(),
            backBtn = wizard.down('#reading-types-bulk-back'),
            nextBtn = wizard.down('#reading-types-bulk-next'),
            confirmBtn = wizard.down('#reading-types-bulk-confirm'),
            finishBtn = wizard.down('#reading-types-bulk-finish'),
            cancelBtn = wizard.down('#reading-types-bulk-cancel');
        nextCmp.name == 'selectReadingTypes' ? backBtn.disable() : backBtn.enable();

        switch (nextCmp.name) {
            case 'selectReadingTypes':
            case 'selectOperation':
            case 'selectActionItems':
                backBtn.show();
                nextBtn.show();
                cancelBtn.show();
                confirmBtn.hide();
                finishBtn.hide();
                break;
            case 'confirmPage':
                backBtn.show();
                nextBtn.hide();
                cancelBtn.show();
                confirmBtn.show();
                finishBtn.hide();
                break;
            case 'statusPage':
                backBtn.hide();
                nextBtn.hide();
                cancelBtn.hide();
                confirmBtn.hide();
                finishBtn.show();
                break;
        }
    },

    goBack: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router');

        router.getRoute('administration/certificates').forward({},
            router.getQueryStringValues()
        );
    }
});
