Ext.define('Imt.controller.SearchItemsBulkAction', {
    extend: 'Ext.app.Controller',

    requires: [
        'Imt.usagepointmanagement.view.bulk.Browse',
        'Imt.usagepointmanagement.view.bulk.Navigation',
        'Imt.usagepointmanagement.view.bulk.Wizard'
    ],

    stores: [
        'Imt.usagepointmanagement.store.ActiveCalendars'
    ],

    refs: [
        {
            ref: 'usagePointsItemsWizard',
            selector: '#usagepointswizard'
        },
        {
            ref: 'navigationMenu',
            selector: '#usagePointsBulkNavigation'
        },
        {
            ref: 'usagePointsGrid',
            selector: '#usagepoints-bulk-step1 usagepoints-selection-grid'
        },
        {
            ref: 'statusPage',
            selector: '#usagepoints-bulk-step5'
        }
    ],

    init: function(){
        this.control({
            'usagepoints-wizard #nextButton': {
                click: this.nextClick
            },
            'usagepoints-wizard #backButton': {
                click: this.backClick
            },
            'usagepoints-wizard #confirmButton': {
                click: this.confirmClick
            },
            'usagepoints-wizard #finishButton': {
                click: this.goBack
            },
            'usagepoints-wizard #wizardCancelButton': {
                click: this.goBack
            }
        });
    },

    showBulkAction: function(){
        var me = this,
            search = me.getController('Imt.controller.Search'),
            searchResults = Ext.getStore('Uni.store.search.Results'),
            widget;

        // in case user forgot to press apply filters on search page we need ensure that search filters state matches search results state
        search.service.applyFilters();
        if (!searchResults.getCount()) {
            this.goBack();
        } else {
            var store = Ext.create('Ext.data.Store', {
                buffered: true,
                pageSize: 100,
                remoteFilter: true,
                model: searchResults.model,
                filters: searchResults.filters.getRange(),
                proxy: searchResults.getProxy()
            });

            // we replace reader to buffered due to our store is buffered
            store.getProxy().setReader(Ext.create('Uni.data.reader.JsonBuffered', store.getProxy().getReader()));

            widget = Ext.widget('usagepoints-bulk-browse', {
                deviceStore: store
            });
            me.getApplication().fireEvent('changecontentevent', widget);
            widget.setLoading();
            store.load({
                callback: function () {
                    widget.setLoading(false);
                }
            });
        }
    },

    nextClick: function () {
        var layout = this.getUsagePointsItemsWizard().getLayout();
        this.changeContent(layout.getNext(), layout.getActiveItem()) && this.getNavigationMenu().moveNextStep();
    },

    backClick: function () {
        var layout = this.getUsagePointsItemsWizard().getLayout(),
            currentCmp = layout.getActiveItem();

        this.changeContent(layout.getPrev(), currentCmp);
        (currentCmp.name !== 'statusPageViewDevices') && this.getNavigationMenu().movePrevStep();
    },


    changeContent: function (nextCmp, currentCmp) {
        var me = this, errorPanel = null,
            router = me.getController('Uni.controller.history.Router'),
            search = me.getController('Imt.controller.Search'),
            wizard = me.getUsagePointsItemsWizard(),
            layout = wizard.getLayout(),
            errorContainer = currentCmp.down('#stepSelectionError');
        switch (currentCmp.name) {
            case 'selectDevices':
                me.allDevices = me.getUsagePointsGrid().isAllSelected();

                if (!me.allDevices) {
                    me.devices = me.getUsagePointsGrid().getSelectionModel().getSelection();
                }
                me.operation = 'addCalendar';
                nextCmp.down('#usagepointsactionselect').setValue({operation: 'addCalendar'});

                errorPanel = currentCmp.down('#step1-errors');
                me.validation = me.allDevices || me.devices.length;
                break;
            case 'selectOperation':
                me.operation = currentCmp.down('#usagepointsactionselect').getValue().operation;
                if (nextCmp.name == 'selectActionItems') {
                    if (me.operation == 'addCalendar') {
                       //do nothing
                    }
                }
                break;
            case 'selectActionItems':
                errorPanel = currentCmp.down('#step3-errors');
                if (me.operation == 'addCalendar') {
                    me.addCalendarFormValues = currentCmp.down('form').getValues();
                    if(Ext.isEmpty(me.addCalendarFormValues.calendar)){
                        me.validation = false;
                    } else {
                        me.validation = true;
                        me.calendarName = currentCmp.down('form').down('#calendar-combo').getDisplayValue();
                        me.fromTime  = currentCmp.down('form').down('#activation-date-values').down('#activation-on').getValue().getTime()
                    }
                }
                break;
        }

        (currentCmp.navigationIndex > nextCmp.navigationIndex) && (me.validation = true);

        if (me.validation) {
            switch (nextCmp.name) {
                case 'confirmPage':
                        nextCmp.showMessage(me.buildConfirmMessage());
                        wizard.down('#confirmButton').enable()
                        break;
            }
            errorPanel && errorPanel.hide();
            errorContainer && errorContainer.hide();
            layout.setActiveItem(nextCmp);
            this.updateButtonsState(nextCmp);
            return true;
        } else {
            errorPanel && errorPanel.show();
            errorContainer && errorContainer.show();
            return false;
        }
    },

    updateButtonsState: function (activePage) {
        var me = this,
            wizard = me.getUsagePointsItemsWizard(),
            backBtn = wizard.down('#backButton'),
            nextBtn = wizard.down('#nextButton'),
            confirmBtn = wizard.down('#confirmButton'),
            finishBtn = wizard.down('#finishButton'),
            falureFinishBtn = wizard.down('#failureFinishButton'),
            cancelBtn = wizard.down('#wizardCancelButton');
        activePage.name == 'selectDevices' ? backBtn.disable() : backBtn.enable();
        switch (activePage.name) {
            case 'selectDevices' :
                backBtn.show();
                nextBtn.show();
                nextBtn.setDisabled(false);
                confirmBtn.hide();
                finishBtn.hide();
                falureFinishBtn.hide();
                cancelBtn.show();
                break;
            case 'selectOperation' :
                backBtn.show();
                nextBtn.show();
                nextBtn.setDisabled(Ext.isEmpty(me.operation));
                confirmBtn.hide();
                finishBtn.hide();
                falureFinishBtn.hide();
                cancelBtn.show();
                break;
            case 'selectActionItems' :
                backBtn.show();
                nextBtn.show();
                nextBtn.enable();
                confirmBtn.hide();
                finishBtn.hide();
                falureFinishBtn.hide();
                cancelBtn.show();
                break;
            case 'confirmPage' :
                backBtn.show();
                nextBtn.hide();
                confirmBtn.show();
                finishBtn.hide();
                falureFinishBtn.hide();
                cancelBtn.show();
                break;
            case 'statusPage' :
                backBtn.hide();
                nextBtn.hide();
                confirmBtn.hide();
                finishBtn.show();
                cancelBtn.hide();
                break;
        }
    },

    goBack: function () {
        var me = this,
            grid = me.getUsagePointsGrid(),
            search = me.getController('Imt.controller.Search'),
            router = me.getController('Uni.controller.history.Router'),
            queryParams;

        if (grid && search.service.searchDomain) {
            queryParams = {
                restore: true
            };
        }

        router.getRoute('search').forward(null, queryParams);
    },

    buildConfirmMessage: function () {
        var me = this,
            message,
            pattern,
            titleText,
            bodyText;

        if (me.allDevices) {
            switch (me.operation) {
                case 'addCalendar':
                    titleText = Uni.I18n.translate('searchItems.bulk.addCalendarToAllDevices.confirmMsg', 'IMT',
                        "Add calendar '{0}' to all devices?", Ext.String.htmlEncode(me.calendarName));
                    break;
            }
        } else {
            switch (me.operation) {
                case 'addCalendar':
                    if (me.devices.length === 1) {
                        pattern = Uni.I18n.translate('searchItems.bulk.addCalendarXTo1Device.confirmMsg', 'IMT', "Add calendar '{0}' to {1} device?")
                    } else {
                        pattern = Uni.I18n.translate('searchItems.bulk.addCalendarXToYDevices.confirmMsg', 'IMT', "Add calendar '{0}' to {1} devices?")
                    }
                    titleText = Ext.String.format(pattern, Ext.String.htmlEncode(me.calendarName), me.devices.length);
                    break;
            }
        }

        switch (me.operation) {
            case 'addCalendar':
                bodyText = Uni.I18n.translate('searchItems.bulk.addCalendarToAllDevices.addMsg', 'IMT', 'The selected devices will use this calendar');
                break;
        }

        message = {
            title: titleText,
            body: bodyText
        };
        return message;
    },

    confirmClick: function () {
        var me = this,
            wizard = me.getUsagePointsItemsWizard(),
            finishBtn = wizard.down('#finishButton'),
            statusPage = me.getStatusPage(),
            scheduleIds = [],
            devicesMRID = [],
            url = '/api/udr/usagepoints/calendars',
            request = {},
            jsonData;

        finishBtn.disable();
        statusPage.removeAll();
        wizard.setLoading(true);

        if (me.operation === 'addCalendar') {
            Ext.each(me.devices, function (item) {
                devicesMRID.push(item.get('mRID'));
            });
            request.action = me.operation;
            if (me.allDevices) {
                var store = me.getUsagePointsGrid().getStore();
                request.filter = store.getProxy().encodeFilters(store.filters.getRange());
            } else {
                request.deviceMRIDs = devicesMRID;
            }
            request.calendarIds = [me.addCalendarFormValues.calendar];
            request.startTime = me.addCalendarFormValues.activateCalendar === 'immediate-activation' ? new Date().getTime() : me.fromTime;
            jsonData = Ext.encode(request);
            Ext.Ajax.request({
                url: url,
                method: 'PUT',
                jsonData: jsonData,
                timeout: 180000,
                success: function (response) {
                    statusPage.showAddCalendarSuccess(
                        me.buildFinalMessage()
                    );
                    finishBtn.enable();
                    wizard.setLoading(false);
                },

                failure: function (response) {
                    var resp = Ext.decode(response.responseText, true);
                    if (resp && resp.message) {
                        me.showStatusMsg(me.buildMessage(resp.message));
                    }
                    finishBtn.enable();
                    wizard.setLoading(false);
                }
            });
        }
        me.nextClick();
    },

    showStatusMsg: function (msg) {
        var me = this;
        me.getStatusPage().add(msg);
    }
    ,

    buildMessage: function (message) {
        var messagePanel = {
            xType: 'panel'
        };

        messagePanel.html = '<h3>' + message + '</h3>';
        return messagePanel;
    },

    buildFinalMessage: function () {
        var me = this,
            message = '',
            finalMessage = '';

        switch (me.operation) {
            case 'addCalendar':
                message = Ext.isEmpty(me.devices)
                    ? Uni.I18n.translate('searchItems.bulk.addCalendarToAllDevices.all1', 'IMT', "to all usage points")
                    : Uni.I18n.translatePlural('searchItems.bulk.addCalendarToAllDevices', me.devices.length, 'IMT',
                    "to {0} usage points",
                    "to {0} usage point",
                    "to {0} usage points"
                );
                finalMessage = Uni.I18n.translate('searchItems.bulk.addCalendarToAllDevices.baseSuccessMsg', 'IMT',
                        "Successfully added calendar '{0}' {1}", [me.calendarName, message]);
                break;
        }

        return finalMessage;
    }


});
