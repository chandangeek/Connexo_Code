package com.energyict.mdc.tasks;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.domain.util.Finder;
import java.util.List;
import java.util.Optional;

@ProviderType
public interface TaskService {

    String COMPONENT_NAME = "CTS";

    public ComTask newComTask(String name);

    public Optional<ComTask> findComTask(long id);

    /**
     * @return a list of all ComTasks created and maintained by the System
     * @since 1.1
     * @see #findAllUserComTasks()
     * @see #findAllComTasks()
     */
    public List<ComTask> findAllSystemComTasks();

    /**
     * @return a list of ComTasks which are created and maintained by the Users
     * @since 1.1
     * @see #findAllSystemComTasks()
     * @see #findAllComTasks()
     */
    public List<ComTask> findAllUserComTasks();

    /**
     * @return a list of <b>all</b> ComTasks
     * @see #findAllUserComTasks
     * @see #findAllSystemComTasks()
     */
    public Finder<ComTask> findAllComTasks();

    public Optional<ProtocolTask> findProtocolTask(long id);

    /**
     * @return a list of <b>all</b> ProtocolTasks, paging is mandatory in this method
     */
    public Finder<ProtocolTask> findAllProtocolTasks();

    public Optional<ComTask> findFirmwareComTask();

}