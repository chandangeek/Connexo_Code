package com.energyict.mdc.device.lifecycle.impl.micro.actions;

import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.issue.share.service.IssueService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.lifecycle.ExecutableActionProperty;
import com.energyict.mdc.device.lifecycle.impl.ServerMicroAction;

import java.util.List;

/**
 * Provides an implementation for the {@link ServerMicroAction} interface
 * that will close all communication and validation issues on a device with the status <i>Won't fix</i>
 *
 * @see {@link com.energyict.mdc.device.lifecycle.config.MicroAction#CLOSE_ALL_ISSUES}
 * Copyrights EnergyICT
 * Date: 23/06/15
 * Time: 15:45
 */
public class CloseAllIssues implements ServerMicroAction {

    private final IssueService issueService;

    public CloseAllIssues(IssueService issueService) {
        this.issueService = issueService;
    }

    @Override
    public void execute(Device device, List<ExecutableActionProperty> properties) {
        List<OpenIssue> openIssues = device.getOpenIssues();
        if (!openIssues.isEmpty()) {
            IssueStatus wontFix = this.issueService.findStatus(IssueStatus.WONT_FIX).get();
            openIssues.stream().forEach(baseIssue ->
                    issueService.getIssueProviders().forEach(provider ->
                            provider.getOpenIssue(baseIssue)
                                    .ifPresent(openIssue -> openIssue.close(wontFix))));
        }
    }
}
