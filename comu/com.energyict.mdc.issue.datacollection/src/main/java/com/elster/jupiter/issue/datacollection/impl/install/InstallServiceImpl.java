package com.elster.jupiter.issue.datacollection.impl.install;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.issue.datacollection.impl.ModuleConstants;
import com.elster.jupiter.issue.datacollection.impl.i18n.TranslationInstaller;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.callback.InstallService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;

@Component(name = "install", service = InstallService.class, property = "name=" + ModuleConstants.COMPONENT_NAME, immediate = true)
public class InstallServiceImpl implements InstallService {
    private volatile IssueService issueService;
    private volatile MessageService messageService;
    private volatile EventService eventService;
    private volatile Thesaurus thesaurus;

    public InstallServiceImpl() {
    }

    @Inject
    public InstallServiceImpl(IssueService issueService, MessageService messageService, NlsService nlsService) {
        setMessageService(messageService);
        setIssueService(issueService);
        setEventService(eventService);
        setNlsService(nlsService);

        install();
    }

    @Override
    public void install() {
        new Installer(issueService, messageService, eventService, thesaurus).install();
        new TranslationInstaller(thesaurus).createTranslations();
    }

    @Reference
    public final void setIssueService(IssueService issueService) {
        this.issueService = issueService;
    }

    @Reference
    public final void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    @Reference
    public final void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    @Reference
    public final void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(ModuleConstants.COMPONENT_NAME, Layer.DOMAIN);
    }
}
