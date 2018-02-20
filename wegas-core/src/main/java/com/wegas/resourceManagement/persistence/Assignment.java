/*
 * Wegas
 * http://wegas.albasim.ch
 *
 * Copyright (c) 2013-2018 School of Business and Engineering Vaud, Comem, MEI
 * Licensed under the MIT License
 */
package com.wegas.resourceManagement.persistence;

import com.wegas.core.persistence.variable.Beanjection;
import com.wegas.core.persistence.views.Views;
import com.wegas.core.persistence.views.WegasJsonView;
import javax.json.bind.annotation.JsonbProperty;
import javax.json.bind.annotation.JsonbTransient;
import javax.persistence.*;

/**
 *
 * @author Benjamin Gerber <ger.benjamin@gmail.com>
 */
@Table(indexes = {
    @Index(columnList = "variableinstance_id"),
    @Index(columnList = "taskinstance_id")
})
@NamedQueries({
    @NamedQuery(
            name = "Assignment.findByResourceInstanceIdAndTaskInstanceId",
            query = "SELECT a FROM Assignment a where a.resourceInstance.id = :resourceInstanceId AND a.taskInstance.id = :taskInstanceId"
    )
})
@Entity
public class Assignment extends AbstractAssignement {

    private static final long serialVersionUID = 1L;

    /**
     *
     */
    @Id
    @GeneratedValue
    @WegasJsonView(Views.IndexI.class)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "taskinstance_id")
    @JsonbTransient
    private TaskInstance taskInstance;

    /**
     *
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "variableinstance_id", nullable = false)
    @JsonbTransient
    private ResourceInstance resourceInstance;

    /**
     *
     */
    public Assignment() {
        super();
    }

    @Override
    public Long getId() {
        return this.id;
    }

    /**
     * @return the ResourceInstance
     */
    @Override
    public ResourceInstance getResourceInstance() {
        return resourceInstance;
    }

    /**
     * @param resourceInstance
     */
    public void setResourceInstance(ResourceInstance resourceInstance) {
        this.resourceInstance = resourceInstance;
    }

    /**
     * @return the taskInstance
     */
    @Override
    public TaskInstance getTaskInstance() {
        return taskInstance;
    }

    /**
     * @param taskInstance
     */
    @JsonbProperty
    public void setTaskInstance(TaskInstance taskInstance) {
        this.taskInstance = taskInstance;
    }

    @Override
    public void updateCacheOnDelete(Beanjection beans) {
        TaskInstance theTask = this.getTaskInstance();
        ResourceInstance theResource = this.getResourceInstance();

        if (theTask != null) {
            theTask = ((TaskInstance) beans.getVariableInstanceFacade().find(theTask.getId()));
            if (theTask != null) {
                theTask.getAssignments().remove(this);
            }
        }
        if (theResource != null) {
            theResource = ((ResourceInstance) beans.getVariableInstanceFacade().find(theResource.getId()));
            if (theResource != null) {
                theResource.getAssignments().remove(this);
            }
        }
    }

}
