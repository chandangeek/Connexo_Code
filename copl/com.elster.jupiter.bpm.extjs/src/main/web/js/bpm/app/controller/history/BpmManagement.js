Ext.define('Bpm.controller.history.BpmManagement', {
    extend: 'Uni.controller.history.Converter',

    rootToken: 'bpmmanagement',
    previousPath: '',
    currentPath: null,



    routeConfig: {
        bpmmanagement: {
            title: 'Bpm management',
            route: 'bpmmanagement',
            disabled: true,
            items: {
                instances: {
                    title: Uni.I18n.translate('bpm.processInstances.title', 'BPM', 'Process instances'),
                    route: 'instances',
                    controller: 'Bpm.controller.ProcessInstances',
                    action: 'showProcessInstances',
                    items: {
                        view: {
                            title: Uni.I18n.translate('bpm.instance', 'BPM', 'Process instance'),
                            route: '{deploymentId}/{instanceId}',
                            controller: 'Bpm.controller.ProcessInstances',
                            action: 'showProcessInstanceOverview',
                            callback: function(route) {
                                this.getApplication().on('viewProcessInstance', function(record) {
                                    var title = Uni.I18n.translate('bpm.instance.overview.title',  'BPM', 'Instance {0} of process \'{1}\'');
                                    route.setTitle(Ext.String.format(title, record.get('id'), record.get('name')));
                                    return true;
                                }, {single: true});

                                return this;
                            }
                        }
                    }
                }
            }
        }
    },

    tokenizePreviousTokens: function () {
        return this.tokenizePath(this.getApplication().getController('Uni.controller.history.EventBus').previousPath);
    }
});