package com.eden.orchid.api.tasks;

import com.eden.orchid.api.registration.Prioritized;

/**
 * A Runnable tailored for executing some task in Orchid. The 'name' of this OrchidTask is used on the command-line for
 * non-Javadoc builds to execute an alternative OrchidTask. The default OrchidTask builds the site once.
 *
 * @since v1.0.0
 * @orchidApi extensible
 */
public abstract class OrchidTask extends Prioritized implements Runnable {

    public OrchidTask(int priority) {
        super(priority);
    }

    /**
     * Return the name of this OrchidTask
     *
     * @return the name of this task
     */
    public abstract String getName();

    /**
     * Return a description of what this OrchidTask does, which is displayed when listing available Tasks.
     *
     * @return the description of this OrchidTask
     */
    public abstract String getDescription();
}
