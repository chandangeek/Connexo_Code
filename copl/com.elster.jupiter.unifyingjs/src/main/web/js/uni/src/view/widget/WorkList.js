/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Uni.view.widget.WorkList', {
    extend: 'Ext.panel.Panel',
    alias: 'widget.work-list',
    layout: 'fit',
    ui: 'tile',
    title: ' ',
    router: null,
    header: {
        ui: 'small'
    },
    workListTypesStore: null,

    initComponent: function () {
        var me = this;
        me.items = [
            {
                xtype: 'dataview',
                itemId: 'dtv-work-list',
                itemSelector: 'a.x-btn.flag-toggle',

                tpl: new Ext.XTemplate(
                    '<table style="margin: 5px 0px 10px 5px; table-layout: fixed; width: 100%;">',
                    '<tpl for=".">',
                    '<tr id="{id}" class="issue">',
                    '<td height="25" width="20" data-qtip="{iconTooltip}">',
                    '<tpl if="icon"><span class="{icon}"/></tpl></td>',
                    '<td data-qtip="{tooltip}" style="overflow: hidden; text-overflow: ellipsis; white-space: nowrap;"><a href="{href}">{title}</a></td>',
                    '</tr>',
                    '</tpl>',
                    '</table>'
                )
            }
        ];
        me.tools = [
            {
                xtype: 'toolbar',
                itemId: 'tlb-combo',
                margin: '0 10 0 0',
                layout: 'fit',
                width: 150,
                items: [
                    {
                        xtype: 'combobox',
                        itemId: 'cbo-work-types',
                        editable: false,
                        hidden: true,
                        displayField: 'name',
                        cls: 'cbo',
                        listeners: {
                            change: function (combo, newValue) {
                                var workItem = combo.findRecordByValue(newValue);
                                me.loadWorkList(workItem);
                            }
                        }
                    }
                ]
            }
        ];
        me.tbar = {
            xtype: 'container',
            itemId: 'count-container'
        };
        me.bbar = {
            xtype: 'container',
            style: 'margin: 0px 0px 0px -5px',
            itemId: 'docked-links-container'
        };
        me.callParent(arguments);
    },

    reload: function () {
        var me = this,
            header = me.getHeader();

        me.loadWorkListTypes();
        if (me.workListTypesStore.count() == 1) {
            header.down('#cbo-work-types').setVisible(false);
            me.loadWorkList(me.workListTypesStore.first());
        } else if (me.workListTypesStore.count() > 1) {
            header.down('#cbo-work-types').setVisible(true);

            var comboWorkTypes = header.down('#cbo-work-types');
            comboWorkTypes.bindStore(me.workListTypesStore);
            comboWorkTypes.select(me.workListTypesStore.first().get('name'));
        }

        me.setTitle('<h3>' + me.configuration.title + '</h3>');
    },

    loadWorkListTypes: function () {
        var me = this,
            workListTypesData = [];

        Ext.Array.each(me.configuration.items, function (item) {
            workListTypesData.push({
                name: item.name,
                type: item.type,
                workItem: item
            });
        });

        me.workListTypesStore = Ext.create('Ext.data.Store', {
            fields: ['name', 'workItem'],
            data: workListTypesData
        });
    },

    loadWorkList: function (workItem) {
        var me = this,
            dockedLinksContainer = me.down('#docked-links-container'),
            countContainer = me.down('#count-container'),
            dataview = me.down('#dtv-work-list');

        me.setLoading();
        Ext.Ajax.request({
            url: workItem.get('workItem').url,
            method: 'GET',
            success: function (response) {
                var responseObject = Ext.decode(response.responseText);

                Ext.suspendLayouts();

                me.setWorkItems(dataview, workItem, responseObject);
                me.setTopLabel(countContainer, workItem, responseObject);
                me.setLinks(dockedLinksContainer, workItem, responseObject);

                Ext.resumeLayouts(true);
                me.doLayout();
                me.setLoading(false);
            },
            failure: function (response) {
                me.setLoading(false);
            }
        });
    },

    setWorkItems: function (dataview, workItem, responseObject) {
        var me = this,
            itemsData = [], itemsStore;

        Ext.Array.each(responseObject.items, function (item) {
            itemsData.push({
                title: item[workItem.get('workItem').titleProperty],
                tooltip: me.getTooltip(workItem, item),
                href: me.getHref(workItem, item),
                icon: me.getIcon(workItem, item),
                iconTooltip: me.getIconTooltip(workItem, item)
            });
        });

        itemsStore = Ext.create('Ext.data.Store', {
            fields: ['title', 'tooltip', 'href', 'icon', 'iconTooltip'],
            data: itemsData
        });
        dataview.bindStore(itemsStore);
    },

    setTopLabel: function (countContainer, workItem, responseObject) {
        var me = this;

        countContainer.removeAll();
        countContainer.add([
            {
                xtype: 'label',
                itemId: 'lbl-top-most',
                style: 'font-weight: normal; margin: 0px 0px 0px 5px',
                text: responseObject.total == 0 ?
                    workItem.get('workItem').topZeroLabel :
                    Ext.String.format(workItem.get('workItem').topLabel, responseObject.total)
            }
        ]);
    },

    setLinks: function (dockedLinksContainer, workItem, responseObject) {
        var me = this;

        dockedLinksContainer.removeAll();
        dockedLinksContainer.add([
            {
                xtype: 'button',
                itemId: 'lnk-assigned-link',
                text: Ext.String.format(me.configuration.assignedToMeLabel, responseObject.totalUserAssigned),
                ui: 'link',
                href: workItem.get('workItem').assignedToMeLink
            },
            {
                xtype: 'button',
                itemId: 'lnk-workgroup-link',
                text: Ext.String.format(me.configuration.myWorkgroupsLabel, responseObject.totalWorkGroupAssigned),
                ui: 'link',

                href: workItem.get('workItem').myWorkgroupsLink
            }
        ]);
    },

    getTooltip: function (workItem, item) {
        var me = this,
            tooltip = '<table>';

        Ext.Array.each(workItem.get('workItem').tooltipProperties, function (tooltipProperty) {
            var name = tooltipProperty.name,
                type = tooltipProperty.type,
                label = tooltipProperty.label;

            if ((type == undefined) || (type == 'string')) {
                tooltip += '<tr><td><b>' + label + ':</b></td>';
                tooltip += '<td>' + item[name] + '</td></tr>';
            } else if (type == 'datetime') {
                tooltip += '<tr><td><b>' + label + ':</b></td>';
                if (item[name] != '') {
                    tooltip += '<td>' + Uni.DateTime.formatDateTimeLong(new Date(Number(item[name]))) + '</td></tr>';
                }
                else {
                    tooltip += '<td>-</td></tr>';
                }
            } else if (type == 'priority') {
                var priority = item[name];
                priority = (priority <= 3) ? Uni.I18n.translate('bpm.task.priority.high', 'UNI', 'High') :
                    (priority <= 7) ? Uni.I18n.translate('bpm.task.priority.medium', 'UNI', 'Medium') :
                        Uni.I18n.translate('bpm.task.priority.low', 'UNI', 'Low')
                tooltip += '<tr><td><b>' + label + ':</b></td>';
                tooltip += '<td>' + priority + '</td></tr>';
            } else if (type == 'alarmOrIssuePriority') {
                var priority = item[name] / 10;
                priority = (priority <= 2) ? Uni.I18n.translate('bpm.task.priority.veryLow', 'UNI', 'Very low') :
                    (priority <= 4) ? Uni.I18n.translate('bpm.task.priority.low', 'UNI', 'Low') :
                    (priority <= 6) ? Uni.I18n.translate('bpm.task.priority.medium', 'UNI', 'Medium') :
                    (priority <= 8) ? Uni.I18n.translate('bpm.task.priority.high', 'UNI', 'High') :
                        Uni.I18n.translate('bpm.task.priority.veryHigh', 'UNI', 'Very high')
                tooltip += '<tr><td><b>' + label + ':</b></td>';
                tooltip += '<td>' + priority + '</td></tr>';
            }
        });
        tooltip += '</table>';
        return tooltip;
    },

    getHref: function (workItem, item) {
        var me = this,
            args = {},
            queryParams = {};

        Ext.Array.each(workItem.get('workItem').routeArguments, function (routeArgument) {
            var name = routeArgument.name,
                property = routeArgument.property;

            args[name] = item[property];
        });
        Ext.Array.each(workItem.get('workItem').queryParams, function (queryParam) {
            var name = queryParam.name,
                value = queryParam.value;

            queryParams[name] = value;
        });
        return me.router.getRoute(workItem.get('workItem').itemRoute).buildUrl(args, queryParams)
    },

    getIcon: function (workItem, item) {
        var me = this,
            userProperty = workItem.get('workItem').userProperty,
            workgroupProperty = workItem.get('workItem').workgroupProperty;

        if ((workItem.get('workItem').type == 'alarms') || (workItem.get('workItem').type == 'issues')) {
            if (item[userProperty] && item[userProperty].name) {
                return 'icon-user';
            }
            return 'icon-users';
        }
        else {
            if ((item[userProperty] && item[userProperty].length > 0) || (typeof(item['userAssignee']) == 'object')) {
                return 'icon-user';
            }

            return 'icon-users';
        }
    },

    getIconTooltip: function (workItem, item) {
        var me = this,
            userProperty = workItem.get('workItem').userProperty,
            workgroupProperty = workItem.get('workItem').workgroupProperty;

        if ((workItem.get('workItem').type == 'alarms') || (workItem.get('workItem').type == 'issues')) {
            if (item[userProperty] && item[userProperty].name) {
                return Uni.I18n.translate('bpm.task.user', 'UNI', 'User');
            }
            return Uni.I18n.translate('bpm.task.workgroup', 'UNI', 'Workgroup');
        }
        else {
            if ((item[userProperty] && item[userProperty].length > 0) || (typeof(item['userAssignee']) == 'object')) {
                return Uni.I18n.translate('bpm.task.user', 'UNI', 'User');
            }
            return Uni.I18n.translate('bpm.task.workgroup', 'UNI', 'Workgroup');
        }
    }
});