/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Mdc.customattributesonvaluesobjects.controller.CustomAttributeSetVersions', {
    extend: 'Ext.app.Controller',

    requires: [
        'Mdc.customattributesonvaluesobjects.service.RouteMap'
    ],

    refs: [
        {
            ref: 'restoreBtn',
            selector: '#centerContainer #custom-attributes-versions-restore-to-default-btn'
        }
    ],

    init: function () {
        this.control({
            '#time-sliced-custom-attribute-set-action-menu-id': {
                moveToEditPage: this.moveToEditPage,
                moveToClonePage: this.moveToClonePage,
                removeVersion: this.removeVersion
            },
            '#custom-attribute-set-versions-setup-id button[action=moveToAddVersionPage]': {
                click: this.moveToAddPage
            },
            '#centerContainer #custom-attribute-set-version-property-form-id': {
                showRestoreAllBtn: this.showRestoreAllBtn
            },
            '#centerContainer': {
                moveToVersionsPage: this.moveToVersionsPage,
                exceedstime: this.showExceedsMessage,
                gaperror: this.showGapErrorMessage
            }
        });
    },

    showErrorMessage: function(title, message) {
        var box = Ext.create('Ext.window.MessageBox', {
                buttons: [
                    {
                        xtype: 'button',
                        text: Uni.I18n.translate('general.close', 'MDC', 'Close'),
                        action: 'close',
                        name: 'close',
                        ui: 'remove',
                        handler: function () {
                            box.close();
                        }
                    }
                ]
            }),
            config = {};

        Ext.apply(config, {
            title: title,
            msg: message,
            modal: false,
            ui: 'message-error',
            icon: 'icon-warning2',
            style: 'font-size: 34px;'
        });

        box.show(config);
    },

    showExceedsMessage: function () {
        var title = Uni.I18n.translate('general.error', 'MDC', 'Error'),
            message = Uni.I18n.translate('timesliced.customattributtesets.timeexceeds', 'MDC', 'Start time exceeds end time');

        this.showErrorMessage(title, message);
    },

    showGapErrorMessage: function(title, message) {
        this.showErrorMessage(title, message);
    },

    moveToVersionsPage: function (type, isSave, acknowledgement) {
        var me = this,
            route = Mdc.customattributesonvaluesobjects.service.RouteMap.getRoute(type, true, 'version'),
            eventBusController = me.getController('Uni.controller.history.EventBus'),
            previousPath = eventBusController.getPreviousPath(),
            previousQueryString = eventBusController.getPreviousQueryString();

        if (!previousPath) {
            this.navigateToRoute(route, type, null, true);
        } else {
            window.location.href = '#' + previousPath + (previousQueryString ? '?' + previousQueryString : '');
        }

        if (isSave) {
            this.getApplication().fireEvent('acknowledge', acknowledgement);
        }
    },

    showRestoreAllBtn: function (value) {
        var restoreBtn = this.getRestoreBtn();
        if (restoreBtn) {
            if (value) {
                restoreBtn.disable();
            } else {
                restoreBtn.enable();
            }
        }
    },

    moveToEditPage: function (type, versionId) {
        var route = Mdc.customattributesonvaluesobjects.service.RouteMap.getRoute(type, true, 'edit');
        this.navigateToRoute(route, type, versionId, false);
    },

    moveToClonePage: function (type, versionId) {
        var route = Mdc.customattributesonvaluesobjects.service.RouteMap.getRoute(type, true, 'clone');
        this.navigateToRoute(route, type, versionId, false);
    },

    removeVersion: function (type, versionId, versionPeriod) {
        var me = this,
            confirmationWindow = Ext.create('Uni.view.window.Confirmation'),
            router = me.getController('Uni.controller.history.Router'),
            routeArguments = router.arguments,
            routeQueryParams = router.queryParams,
            cpsId = routeArguments.customAttributeSetId || routeQueryParams.customAttributeSetId,
            url = "";

        switch(type){
            case "device":
                var deviceName = routeArguments.deviceId;
                url = '/api/ddr/devices/' + deviceName;
            break;
            case "channel":
                var deviceName = routeArguments.deviceId;
                var channelId = routeArguments.channelId;
                url = '/api/ddr/devices/' + deviceName + '/channels/' + channelId;
            break;
            case "register":
                var deviceName = routeArguments.deviceId;
                var registerId = routeArguments.registerId;
                url = '/api/ddr/devices/' + deviceName + '/registers/' + registerId;
            break;
            case "usagePoint":
                var usagePointName = usagePointId;
                url = '/api/udr/usagepoints/' + usagePointName;
            break;
            default:
            break;
        }
        if (url){
            url += '/customproperties/' + cpsId + '/versions/' + versionId;
        }
        confirmationWindow.show(
            {
                msg: Uni.I18n.translate('sapattribute.removeVersionText', 'MDC', 'This version will no longer be available.'),
                title: Uni.I18n.translate('sapattribute.removeVersionTitle', 'MDC', 'Remove {0} version?', versionPeriod),
                fn: function (state) {
                    if (state === 'confirm') {
                        Ext.Ajax.request({
                            url: url,
                            method: 'DELETE',
                            success: function (response) {
                                var messageText = Uni.I18n.translate('sapattribute.succesfullyRemoved', 'MDC', '{0} version removed', versionPeriod)
                                me.getApplication().fireEvent('acknowledge', messageText);
                            },
                            callback: function(){
                                router.getRoute().forward();
                            }
                        });
                    }
                }
            });
    },

    moveToAddPage: function (button) {
        var route = Mdc.customattributesonvaluesobjects.service.RouteMap.getRoute(button.type, true, 'add');
        this.navigateToRoute(route, button.type, null, false)
    },

    navigateToRoute: function (route, type, versionId, isVersions) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            routeArguments = router.arguments,
            routeQueryParams = router.queryParams;

        if (type === 'device') {
            if (isVersions) {
                routeQueryParams.customAttributeSetId = routeArguments.customAttributeSetId;
            } else {
                routeArguments.customAttributeSetId = routeQueryParams.customAttributeSetId;
                routeQueryParams = {}
            }
        }

        if (!Ext.isEmpty(versionId)) {
            routeArguments.versionId = versionId;
        }

        router.getRoute(route).forward(routeArguments, routeQueryParams);
    }
});