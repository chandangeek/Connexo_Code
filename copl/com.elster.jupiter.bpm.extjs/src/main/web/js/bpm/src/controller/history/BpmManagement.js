/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Bpm.controller.history.BpmManagement', {
    extend: 'Uni.controller.history.Converter',
    requires: [
        'Bpm.privileges.BpmManagement'
    ],
    rootToken: 'workspace',
    previousPath: '',
    currentPath: null,

    routeConfig: {
        administration: {
            title: Uni.I18n.translate('general.administration', 'BPM', 'Administration'),
            route: 'administration',
            disabled: true,
            items: {
                managementprocesses:{
                    title: Uni.I18n.translate('bpm.process.title', 'BPM', 'Processes'),
                    route: 'managementprocesses',
                    controller: 'Bpm.processes.controller.Processes',
                    action: 'showProcesses',
                    privileges: Bpm.privileges.BpmManagement.viewProcesses,
                    items: {
                        edit: {
                            title: Uni.I18n.translate('bpm.editprocess.title', 'BPM', 'Edit process'),
                            route: '{name}/{version}/edit',
                            controller: 'Bpm.processes.controller.Processes',
                            privileges: Bpm.privileges.BpmManagement.administrateProcesses,
                            action: 'editProcess',
                            params: {
                                activate: false
                            },
                            callback: function (route) {
                                this.getApplication().on('editProcesses', function (name) {
                                    route.setTitle(Uni.I18n.translate('editProcess.edit', 'BPM', "Edit '{0}'", name));
                                    return true;
                                }, {single: true});
                                return this;
                            }
                        },
                        activate: {
                            title: Uni.I18n.translate('bpm.editprocess.title', 'BPM', 'Edit process'),
                            route: '{name}/{version}/activate',
                            controller: 'Bpm.processes.controller.Processes',
                            privileges: Bpm.privileges.BpmManagement.administrateProcesses,
                            action: 'activateProcess',
                            params: {
                                activate: true
                            },
                            callback: function (route) {
                                this.getApplication().on('activateProcesses', function (name) {
                                    route.setTitle(Uni.I18n.translate('editProcess.activate', 'BPM', "Activate '{0}'", name));
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