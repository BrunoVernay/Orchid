package com.eden.orchid.impl.tasks;

import com.caseyjbrooks.clog.Clog;
import com.eden.orchid.api.resources.OrchidResourceSource;
import com.eden.orchid.api.tasks.OrchidTask;
import com.eden.orchid.api.tasks.OrchidTasks;
import com.eden.orchid.impl.resources.DefaultResourceSource;
import com.eden.orchid.impl.resources.LocalResourceSource;
import com.eden.orchid.utilities.ObservableTreeSet;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Set;

@Singleton
public class ListResourceSourcesTask extends OrchidTask {

    private Set<OrchidResourceSource> sources;

    @Inject
    public ListResourceSourcesTask(Set<LocalResourceSource> localResourceSources, Set<DefaultResourceSource> defaultResourceSources) {
        this.sources = new ObservableTreeSet<>();

        this.sources.addAll(localResourceSources);
        this.sources.addAll(defaultResourceSources);
    }

    @Override
    public String getName() {
        return "listSources";
    }

    @Override
    public String getDescription() {
        return "Display all available OrchidResourceSources, or which plugins are searched for resource files.";
    }

    @Override
    public void run() {
        Clog.logger(OrchidTasks.loggerKey, "" +
                "#{ $0 | fg('cyan') }[Priority]#{$0 |reset}" +
                "#{ $0 | fg('magenta') }[Source]#{$0 |reset}" +
                "");
        Clog.logger(OrchidTasks.loggerKey, "------------------------------------------------------------------------------------");
        Clog.logger(OrchidTasks.loggerKey, "------------------------------------------------------------------------------------");

        for (OrchidResourceSource source : new ObservableTreeSet<>(sources)) {

            String message;

            if (source.getPriority() < 0) {
                message = "#{ $0 | fg('red') }[#{$1}][#{$2}]";
                message += "(inactive)#{$0 |reset}";
            }
            else {
                message = "#{ $0 | fg('cyan') }[#{$1}]#{$0 |reset}";
                message += "#{ $0 | fg('magenta') }[#{$2}]#{$0 |reset}";
            }

            Clog.logger(OrchidTasks.loggerKey, message, new Object[]{source.getPriority(), source.getClass().getName()});
        }
    }
}
