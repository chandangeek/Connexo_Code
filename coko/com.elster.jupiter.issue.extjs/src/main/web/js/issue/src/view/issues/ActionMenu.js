Ext.define('Isu.view.issues.ActionMenu', {
    extend: 'Ext.menu.Menu',
    alias: 'widget.issues-action-menu',
    store: 'Isu.store.IssueActions',
    plain: true,
    border: false,
    shadow: false,
    defaultAlign: 'tr-br?',
    minHeight: 60,
    router: null,
    mixins: {
        bindable: 'Ext.util.Bindable'
    },
    predefinedItems: [
        {
            text: Uni.I18n.translate('issues.actionMenu.addComment', 'ISU', 'Add comment'),
            hidden: Uni.Auth.hasNoPrivilege('privilege.comment.issue'),
            action: 'addComment'
        }
    ],
    listeners: {
        show: {
            fn: function () {
                var me = this;

                me.removeAll();
                if (me.record) {
                    me.store.getProxy().url = me.record.getProxy().url + '/' + me.record.getId() + '/actions';
                    me.store.load(function () {
                        me.onLoad();
                        me.setLoading(false);
                    });
                    setTimeout(function () {
                        me.setLoading(true);
                    }, 1)
                } else {
                    //<debug>
                    console.error('Record for \'' + me.xtype + '\' is not defined');
                    //</debug>
                }
            }
        }
    },
    initComponent: function () {
        var me = this;

        //<debug>
        if (!me.router) {
            console.error('Router for \'' + me.xtype + '\' is not defined');
        }
        //</debug>

        me.bindStore(me.store || 'ext-empty-store', true);

        this.callParent(arguments);
    },

    onLoad: function () {
        var me = this,
            deviceMRID,
            comTaskId,
            comTaskSessionId,
            connectionTaskId,
            comSessionId;

        if (!me.router) {
            return
        }

        me.removeAll();

        // add dynamic actions
        me.store.each(function (record) {
            var isHidden = false;
            switch (record.get('name')) {
                case 'Assign issue':
                    isHidden = Uni.Auth.hasNoPrivilege('privilege.assign.issue');
                    break;
                case 'Close issue':
                    isHidden = Uni.Auth.hasNoPrivilege('privilege.close.issue');
                    break;
                case 'Retry now':
                    isHidden = Uni.Auth.hasNoPrivilege('privilege.view.scheduleDevice');
                    break;
                case 'Send someone to inspect':
                    isHidden = Uni.Auth.hasNoPrivilege('privilege.action.issue');
                    break;
                case 'Notify user':
                    isHidden = Uni.Auth.hasNoPrivilege('privilege.action.issue');
                    break;
            }

            var menuItem = {
                text: record.get('name'),
                hidden: isHidden
            };

            if (Ext.isEmpty(record.get('parameters'))) {
                menuItem.actionRecord = record;
            } else {
                menuItem.href = me.router.getRoute(me.router.currentRoute.replace('/view', '') + '/view/action').buildUrl({issueId: me.record.getId(), actionId: record.getId()});
            }
            me.add(menuItem);
        });

        // add predefined actions
        if (me.predefinedItems && me.predefinedItems.length) {
            Ext.Array.each(me.predefinedItems, function (menuItem) {
                switch (menuItem.action) {
                    case 'addComment':
                        delete menuItem.action;
                        menuItem.href = me.router.getRoute(me.router.currentRoute.replace('/view', '') + '/view').buildUrl({issueId: me.record.getId()}, {addComment: true});
                        break;
                }
            });
            me.add(me.predefinedItems);
        }

        // add specific actions
        if (Uni.Auth.hasAnyPrivilege(['privilege.administrate.device','privilege.view.device'])) {
            deviceMRID = me.record.get('deviceMRID');
            if (deviceMRID) {
                comTaskId = me.record.get('comTaskId');
                comTaskSessionId = me.record.get('comTaskSessionId');
                connectionTaskId = me.record.get('connectionTaskId');
                comSessionId = me.record.get('comSessionId');
                if (comTaskId && comTaskSessionId) {
                    me.add({
                        text: Uni.I18n.translate('issues.actionMenu.viewCommunicationLog', 'ISU', 'View communication log'),
                        href: me.router.getRoute('devices/device/communicationtasks/history/viewlog').buildUrl(
                            {
                                mRID: deviceMRID,
                                comTaskId: comTaskId,
                                historyId: comTaskSessionId
                            },
                            {
                                filter: {
                                    logLevels: ['Error', 'Warning', 'Information']
                                }
                            }
                        ),
                        hrefTarget: '_blank'
                    });
                }
                if (connectionTaskId && comSessionId) {
                    me.add({
                        text: Uni.I18n.translate('issues.actionMenu.viewConnectionLog', 'ISU', 'View connection log'),
                        href: me.router.getRoute('devices/device/connectionmethods/history/viewlog').buildUrl(
                            {
                                mRID: deviceMRID,
                                connectionMethodId: connectionTaskId,
                                historyId: comSessionId
                            },
                            {
                                filter: {
                                    logLevels: ['Error', 'Warning', 'Information'],
                                    logTypes: ['connections', 'communications']
                                }
                            }
                        ),
                        hrefTarget: '_blank'
                    });
                }
            }
        }
    }
});