Ext.define('Imt.usagepointmanagement.view.UsagePointMetrologyConfig', {
    extend: 'Ext.form.Panel',
    alias: 'widget.usage-point-metrology-config',
    layout: 'form',
    defaults: {
        xtype: 'displayfield',
        labelWidth: 120
    },

    router: null,

    initComponent: function () {
        var me = this;

        me.items = [
            {
                itemId: 'up-metrology-config-name',
                name: 'name',
                fieldLabel: Uni.I18n.translate('general.name', 'IMT', 'Name'),
                renderer: function (value) {
                    var result = '',
                        record = me.getRecord(),
                        canViewMetrologyConfig,
                        activationTime;

                    if (record) {
                        canViewMetrologyConfig = Imt.privileges.MetrologyConfig.canView();
                        activationTime = record.get('activationTime');
                        if (value) {
                            if (canViewMetrologyConfig) {
                                result += '<a href="'
                                    + me.router.getRoute('administration/metrologyconfiguration/view').buildUrl({mcid: record.getId()})
                                    + '">';
                            }
                            result += value;
                            if (canViewMetrologyConfig) {
                                result += '</a>';
                            }
                        }
                        if (activationTime) {
                            result += '<br><span style="font-size: 90%">'
                                + Uni.I18n.translate('general.fromDate.lc', 'IMT', 'from {0}', [Uni.DateTime.formatDateTimeShort(new Date(activationTime))], false)
                                + '</span>';
                        }
                    }

                    return result || '-';
                }
            },
            {
                itemId: 'up-metrology-config-empty',
                fieldLabel: ' ',
                hidden: true,
                htmlEncode: false,
                renderer: function () {
                    var url = me.router.getRoute('usagepoints/view/definemetrology').buildUrl({},{fromLandingPage: true});
                    return Uni.I18n.translate('general.label.defineConfiguration', 'IMT', '<a href="{0}">Define configuration</a>',url);
                },
                listeners: {
                    beforerender: function() {
                        if (!me.getRecord().get('name') && Imt.privileges.UsagePoint.canAdministrate()) {
                            this.show();
                        }
                    }
                }
            },
            {
                itemId: 'up-metrology-config-purposes',
                xtype: 'fieldcontainer'
            },
            {
                itemId: 'up-metrology-config-meters',
                xtype: 'fieldcontainer'
            },
            {
                itemId: 'up-metrology-config-meters-empty',
                fieldLabel: ' ',
                hidden: true,
                htmlEncode: false,
                renderer: function () {
                    var url = me.router.getRoute('usagepoints/view/metrologyconfiguration/activatemeters').buildUrl({},{fromLandingPage: true});
                    return Uni.I18n.translate('general.label.setMeters', 'IMT', '<a href="{0}">Set meters</a>',url);
                }
            }
        ];

        me.bbar = [
            {
                itemId: 'up-metrology-config-more-details-link',
                ui: 'link',                
                text: Uni.I18n.translate('general.moreDetails', 'IMT', 'More details'),
                href: me.router.getRoute('usagepoints/view/metrologyconfiguration').buildUrl()
            }
        ];

        me.callParent(arguments);

        me.performWidget();
    },


    performWidget: function(){
        var me = this;

        Ext.suspendLayouts();
        me.loadMeterActivations();
        me.loadPurposes();
        Ext.resumeLayouts(true);
    },

    loadPurposes: function(){
        var me = this,
            purposesContainer = me.down('#up-metrology-config-meters'),
            first = true,
            purposes;
        if(me.usagePoint.get('metrologyConfiguration')){
            purposes = me.usagePoint.get('metrologyConfiguration').purposes
        }

        if (!Ext.isEmpty(purposes)) {
            Ext.Array.each(purposes, function(purpose){
                if(purpose.active){
                    purposesContainer.add({
                        xtype: 'displayfield',
                        labelWidth: 120,
                        margin: first ? 0 : '-13 0 0 0',
                        fieldLabel: first ? Uni.I18n.translate('general.label.activePurposes', 'IMT', 'Active purposes') : '&nbsp;',
                        value: purpose.name,
                        renderer: function(value){
                            var icon = '&nbsp;&nbsp;&nbsp;&nbsp;<i class="icon '
                                    + (purpose.status.id == 'incomplete' ? 'icon-warning2' : 'icon-checkmark-circle2')
                                    + '" style="display: inline-block; width: 16px; height: 16px;" data-qtip="'
                                    + purpose.status.name
                                    + '"></i>',
                                url = me.router.getRoute('usagepoints/view/purpose').buildUrl({purposeId: purpose.id}),
                                link = '<a href="' + url + '">' + Ext.String.htmlEncode(value) + '</a>';
                            return link + icon;
                        }
                    });
                    first = false;
                }
            });
        }
        if(first) {
            purposesContainer.add({
                xtype: 'displayfield',
                labelWidth: 120,
                fieldLabel: Uni.I18n.translate('general.label.activePurposes', 'IMT', 'Active purposes'),
                value: '-'
            })
        }
    },

    loadMeterActivations: function(){
        var me = this,
            first = true,
            metersContainer = me.down('#up-metrology-config-meters'),
            store = me.meterActivationsStore,
            mRID = me.usagePoint.get('mRID'),
            makeWGOTooltip = function(watsGoingOnMeterStatus){
                var result = '',
                    first = true;
                if(watsGoingOnMeterStatus.openIssues){
                    result += Uni.I18n.translate('general.label.openIssues', 'IMT', 'Open issues({0})', watsGoingOnMeterStatus.openIssues);
                    first = false;
                }
                if(watsGoingOnMeterStatus.ongoingProcesses){
                    if(!first){
                        result += '<br>';
                    }
                    first = false;
                    result += Uni.I18n.translate('general.label.ongoingProcesses', 'IMT', 'Ongoing processes({0})', watsGoingOnMeterStatus.ongoingProcesses);
                }
                if(watsGoingOnMeterStatus.ongoingServiceCalls){
                    if(!first){
                        result += '<br>';
                    }
                    first = false;
                    result += Uni.I18n.translate('general.label.ongoingServiceCalls', 'IMT', 'Ongoing service calls({0})', watsGoingOnMeterStatus.ongoingServiceCalls);
                }

                return {result: result, status: !first};

            };
        store.setMrid(mRID);

        store.load({
            callback: function(){
                store.filter(function(item){
                    if(item.get('meter')){
                        return item;
                    }
                });
                var count = store.getCount();
                if (count && count <= 2) {
                    store.each(function (meterActivation) {
                        if(meterActivation.get('meter')){
                            metersContainer.add({
                                xtype: 'displayfield',
                                labelWidth: 120,
                                fieldLabel: first ? Uni.I18n.translate('general.meters', 'IMT', 'Meters') : '&nbsp;',
                                value: meterActivation.get('meter'),
                                renderer: function (value) {
                                    var result = '',
                                        gotConfig = makeWGOTooltip(value.watsGoingOnMeterStatus),
                                        tooltip = gotConfig.result,
                                        icon = '&nbsp;&nbsp;&nbsp;&nbsp;<i class="icon '
                                            + 'icon-warning2'
                                            + '" style="display: inline-block; width: 16px; height: 16px;" data-qtip="'
                                            + tooltip
                                            + '"></i>',
                                        url = Ext.String.format('{0}/devices/{1}', Uni.store.Apps.getAppUrl('MultiSense'), encodeURIComponent(value.mRID)),
                                        link = '<a href="' + url + '">' + Ext.String.htmlEncode(value.mRID) + '</a>',
                                        activationTime = meterActivation.get('meterRole').activationTime;
                                    result += gotConfig.status ? link + icon : link;

                                    if (activationTime) {
                                        result += '<br><span style="font-size: 90%">'
                                            + Uni.I18n.translate('general.fromDate.lc', 'IMT', 'from {0}', [Uni.DateTime.formatDateTimeShort(new Date(activationTime))], false)
                                            + '</span>';
                                    }

                                    return result;
                                }
                            });
                            first = false;
                        }
                    });
                } else if (count && count > 2){
                    metersContainer.add({
                        xtype: 'displayfield',
                        labelWidth: 120,
                        fieldLabel: Uni.I18n.translate('general.label.meters', 'IMT', '{0} meters', count),
                        value: '-'
                    });
                } else if (!count) {
                    metersContainer.add({
                        xtype: 'displayfield',
                        labelWidth: 120,
                        fieldLabel: Uni.I18n.translate('general.label.meters', 'IMT', 'Meters'),
                        value: '-'
                    });
                    if(me.usagePoint.get('metrologyConfiguration')){
                        me.down('#up-metrology-config-meters-empty').show();
                    }
                }
            }
        })
    }
});