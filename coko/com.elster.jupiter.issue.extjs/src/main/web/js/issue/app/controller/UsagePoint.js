Ext.define('Mtr.controller.UsagePoint', {
    extend: 'Ext.app.Controller',

    stores: [
        'UsagePoints',
        'Readings',
        'mock.Browsers',
        'mock.Gapped'
    ],

    models: [
        'UsagePoint'
    ],

    views: [
        'error.Page',
        'usagepoint.List',
        'usagepoint.Browse',
        'usagepoint.Search',
        'usagepoint.Edit'
    ],

    refs: [
        {
            ref: 'piechart',
            selector: 'usagePointBrowse #piechart'
        },
        {
            ref: 'linechart',
            selector: 'usagePointBrowse #linechart'
        }
    ],

    usagePointEditWindow: null,

    init: function () {
        this.initMenu();

        this.control({
            'usagePointBrowse': {
                editgeneralinfo: this.editGeneralInfo,
                edittechinfo: this.editTechInfo,
                editotherinfo: this.editOtherInfo
            },
            'usagePointEdit button[action=update]': {
                click: this.updateUsagePoint
            },
            'usagePointList button[action=save]': {
                click: this.saveUsagePoints
            },
            'usagePointSearch button[action=search]': {
                click: this.searchUsagePoints
            }
        });
    },

    initMenu: function () {
        var menuItem = Ext.create('Uni.model.MenuItem', {
            text: 'Usage points',
            href: Mtr.getApplication().getHistoryUsagePointController().tokenizeShowOverview(),
            glyph: 'xe01f@icomoon'
        });

        Uni.store.MenuItems.add(menuItem);
    },

    showOverview: function () {
        var me = this,
            widget = Ext.widget('usagePointList');
        me.getApplication().fireEvent('changecontentevent', widget);
    },
    browse: function (id) {
        var me = this;

        Mtr.model.UsagePoint.load(id, {
            callback: function (usagePoint) {
                if (usagePoint !== null) {
                    me.browseUsagePoint(usagePoint);
                } else {
                    // TODO Show a specific error page.
                    var widget = Ext.widget('errorPage');
                    me.getApplication().fireEvent('changecontentevent', widget);
                }
            }
        });
    },

    browseUsagePoint: function (usagePoint) {
        var widget = Ext.widget('usagePointBrowse');
        Mtr.getApplication().getMainController().showContent(widget);

        this.initBreadcrumbItems(widget, usagePoint);
        this.initGeneralInfo(widget, usagePoint);
        this.initTechInfo(widget, usagePoint);
        this.initOtherInfo(widget, usagePoint);
        this.initReadingsChart(usagePoint);
    },

    initBreadcrumbItems: function (widget, usagePoint) {
        var items = [
            {
                label: 'Usage points',
                href: Mtr.getApplication().getHistoryUsagePointController().tokenizeShowOverview()
            },
            {
                label: 'Usage point ' + usagePoint.data.id
            }
        ];

        widget.setBreadcrumbItems(items);
    },

    initGeneralInfo: function (widget, usagePoint) {
        var properties = [],
            id = usagePoint.data.id,
            name = usagePoint.data.name,
            mRID = usagePoint.data.mRID,
            description = usagePoint.data.description,
            serviceCategory = usagePoint.data.serviceCategory,
            serviceLocation = usagePoint.data.serviceLocation,
            address = null;

        properties['Name'] = this.getValueUnlessEmpty(name);
        properties['mRID'] = this.getValueUnlessEmpty(mRID);
        properties['Description'] = this.getValueUnlessEmpty(description);
        properties['Service category'] = this.getValueUnlessEmpty(serviceCategory);
        properties['Location'] = 'Unknown';

        if (serviceLocation != null && serviceLocation.mainAddress != null) {
            // TODO The format possibly changes per country: http://en.wikipedia.org/wiki/Address_(geography)
            var streetDetail = serviceLocation.mainAddress.streetDetail,
                townDetail = serviceLocation.mainAddress.townDetail,
                streetInfo = this.getStreetInfoFromDetail(streetDetail),
                townInfo = this.getTownInfoFromDetail(townDetail);

            address = streetInfo + ', ' + townInfo + ', ';
            address += townDetail.stateOrProvince + ', ' + townDetail.country;

            var location = streetInfo + '<br />' + townInfo + '<br />';
            if (townDetail.stateOrProvince != null) {
                location += townDetail.stateOrProvince + '<br />';
            }

            properties['Location'] = location + townDetail.country;
        }

        widget.setGeneralInfo(id, properties);
        widget.setGeneralMapLocation(name, address);
    },
    getStreetInfoFromDetail: function (streetDetail) {
        return streetDetail.name + ' ' + streetDetail.number;
    },
    getTownInfoFromDetail: function (townDetail) {
        return townDetail.code + ' ' + townDetail.name;
    },

    initTechInfo: function (widget, usagePoint) {
        var properties = [],
            id = usagePoint.data.id,
            grounded = usagePoint.data.grounded,
            minimalUsageExpected = usagePoint.data.minimalUsageExpected,
            nominalServiceVoltage = this.formatQuantityField(usagePoint.data.nominalServiceVoltage),
            estimatedLoad = this.formatQuantityField(usagePoint.data.estimatedLoad),
            ratedCurrent = this.formatQuantityField(usagePoint.data.ratedCurrent),
            ratedPower = this.formatQuantityField(usagePoint.data.ratedPower),
            readCycle = usagePoint.data.readCycle,
            readRoute = usagePoint.data.readRoute;

        properties['Grounded'] = this.getValueUnlessEmpty(grounded);
        properties['Minimal usage expected'] = this.getValueUnlessEmpty(minimalUsageExpected);
        properties['Nominal service voltage'] = this.getValueUnlessEmpty(nominalServiceVoltage);
        properties['Estimated load'] = this.getValueUnlessEmpty(estimatedLoad);
        properties['Rated current'] = this.getValueUnlessEmpty(ratedCurrent);
        properties['Rated power'] = this.getValueUnlessEmpty(ratedPower);
        properties['Read cycle'] = this.getValueUnlessEmpty(readCycle);
        properties['Read route'] = this.getValueUnlessEmpty(readRoute);

        widget.setTechInfo(id, properties);
    },

    formatQuantityField: function (quantity) {
        if (quantity && quantity.value && quantity.unit) {
            return quantity.value + ' ' + quantity.unit;
        } else {
            return '';
        }
    },

    initOtherInfo: function (widget, usagePoint) {
        // TODO Init other information.
        var properties = [],
            id = usagePoint.data.id,
            version = usagePoint.data.version,
            billingReady = usagePoint.data.amiBillingReady,
            isSdp = usagePoint.data.isSdp,
            isVirtual = usagePoint.data.isVirtual,
            createTime = usagePoint.data.createTime,
            modTime = usagePoint.data.modTime;

        properties['Version'] = this.getValueUnlessEmpty(version);
        properties['Billing ready'] = this.getValueUnlessEmpty(billingReady);
        properties['Service delivery point'] = this.getValueUnlessEmpty(isSdp);
        properties['Virtual'] = this.getValueUnlessEmpty(isVirtual);
        properties['Creation time'] = this.getValueUnlessEmpty(createTime);
        properties['Modification time'] = this.getValueUnlessEmpty(modTime);

        widget.setOtherInfo(id, properties);
    },

    getValueUnlessEmpty: function (value) {
        if (value != null && value != '') {
            if (value instanceof Date) {
                return value.toUTCString();
            }
            return value;
        }
        return '';
    },

    initReadingsChart: function (usagePoint) {
        var me = this,
            store = me.getReadingsStore();

        store.load({
            params: {
                id: usagePoint.data.id
            },
            callback: function () {
                if (store.getCount() > 0) {
                    me.getLinechart().bindStore(store);
                }
            }
        });
    },

    editGeneralInfo: function (id) {
        var me = this;

        Mtr.model.UsagePoint.load(id, {
            callback: function (usagePoint) {
                if (usagePoint !== null) {
                    me.editUsagePoint(usagePoint);
                    me.usagePointEditWindow.showGeneralInfo();
                }
            }
        });
    },

    editTechInfo: function (id) {
        var me = this;

        Mtr.model.UsagePoint.load(id, {
            callback: function (usagePoint) {
                if (usagePoint !== null) {
                    me.editUsagePoint(usagePoint);
                    me.usagePointEditWindow.showTechInfo();
                }
            }
        });
    },

    editOtherInfo: function (id) {
        var me = this;

        Mtr.model.UsagePoint.load(id, {
            callback: function (usagePoint) {
                if (usagePoint !== null) {
                    me.editUsagePoint(usagePoint);
                    me.usagePointEditWindow.showOtherInfo();
                }
            }
        });
    },

    editUsagePoint: function (usagePoint) {
        this.usagePointEditWindow = Ext.widget('usagePointEdit');
        this.usagePointEditWindow.showUsagePoint(usagePoint);
    },

    updateUsagePoint: function () {
        var me = this,
            editWindow = me.usagePointEditWindow,
            usagePoint = editWindow.getUsagePoint(),
            values = editWindow.getValues();

        usagePoint.set(values);
        usagePoint.save({
            callback: function () {
                usagePoint.commit();

                editWindow.close();
                me.browseUsagePoint(usagePoint);
            }
        });
    },

    saveSuccess: function () {
        //alert('Saved');
    },

    saveFailed: function () {
        alert('Failed');
    },

    saveUsagePoints: function () {
        this.getUsagePointsStore().sync({ success: this.saveSuccess, failure: this.saveFailed });
    },

    searchUsagePoints: function (button) {
        var panel = button.up('panel') ,
            viewport = panel.up('viewport'),
            form = panel.down('form');
        this.getUsagePointsStore().load({ params: form.getValues() });
        viewport.removeAll();
        viewport.add({xtype: 'usagePointList'});
        //var grid = Ext.create('Mtr.view.usagepoint.List');
        //viewport.add(grid);
    }
});
