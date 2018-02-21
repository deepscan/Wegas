/*
 * Wegas
 * http://wegas.albasim.ch
 *
 * Copyright (c) 2013-2018 School of Business and Engineering Vaud, Comem, MEI
 * Licensed under the MIT License
 */
package com.wegas.resourceManagement.persistence;

import com.wegas.core.exception.client.WegasIncompatibleType;
import com.wegas.core.persistence.AbstractEntity;
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
@Entity

@Table(indexes = {
    @Index(columnList = "variableinstance_id"),
    @Index(columnList = "taskinstance_id"),
    @Index(columnList = "wrequirement_id")
})
public class Activity extends AbstractAssignement {

    private static final long serialVersionUID = 1L;

    @Transient
    @JsonbTransient
    private String deserialisedRequirementName;

    /**
     *
     */
    @Id
    @GeneratedValue
    @WegasJsonView(Views.IndexI.class)
    private Long id;
    /**
     * worked time ? strange spelling...
     */
    @Column(name = "wtime")
    private double time;

    /**
     * Start time
     */
    @Column(name = "stime")
    private double sTime;

    /**
     *
     */
    @Column(name = "wcompletion")
    private double completion;
    /**
     *
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "taskinstance_id")
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
    @ManyToOne(optional = true, cascade = CascadeType.ALL)
    @JoinColumn(name = "wrequirement_id", nullable = true)
    @JsonbTransient
    private WRequirement requirement;

    /**
     *
     */
    public Activity() {
        super();
        this.time = 0.0D;
        this.completion = 0.0D;
        this.requirement = null;
    }

    /**
     *
     * @param a
     */
    @Override
    public void merge(AbstractEntity a) {
        if (a instanceof Activity) {
            Activity other = (Activity) a;
            super.merge(other);

            this.setTime(other.getTime());
            this.setStartTime(other.getStartTime());
            this.setCompletion(other.getCompletion());

            this.setRequirementName(other.getRequirementName());
        } else {
            throw new WegasIncompatibleType(this.getClass().getSimpleName() + ".merge (" + a.getClass().getSimpleName() + ") is not possible");
        }
    }

    @Override
    public Long getId() {
        return this.id;
    }

    /**
     * @return the ResourceInstance
     */
    @JsonbTransient
    @Override
    public ResourceInstance getResourceInstance() {
        return resourceInstance;
    }

    /**
     * @param resourceInstance
     */
    @JsonbTransient
    public void setResourceInstance(ResourceInstance resourceInstance) {
        this.resourceInstance = resourceInstance;
    }

    /**
     * @return the time
     */
    public double getTime() {
        return time;
    }

    /**
     * @param time the time to set
     */
    public void setTime(double time) {
        this.time = time;
    }

    /**
     *
     * @return the start time (Period.Step)
     */
    public double getStartTime() {
        return sTime;
    }

    /**
     * Set startTime
     *
     * @param sTime
     */
    public void setStartTime(double sTime) {
        this.sTime = sTime;
    }

    /**
     * @return the taskInstance
     */
    @JsonbTransient
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

    /**
     * @return the completion
     */
    public double getCompletion() {
        return completion;
    }

    /**
     * @param completion the completion to set
     */
    public void setCompletion(double completion) {
        this.completion = completion;
    }

    /**
     * @return the requirement
     */
    public WRequirement getRequirement() {
        return requirement;
    }

    /**
     * Unique req name
     *
     * @return unique id for the requirement
     */
    public String getRequirementName() {
        if (requirement != null) {
            return requirement.getName();
        } else {
            return deserialisedRequirementName;
        }
    }

    /**
     * Set requirement name
     *
     * @param name
     */
    public void setRequirementName(String name) {
        this.deserialisedRequirementName = name;
    }

    /**
     * @param requirement the requirement to set
     */
    public void setRequirement(WRequirement requirement) {
        this.requirement = requirement;
        if (requirement != null) {
            this.deserialisedRequirementName = null;
        }
    }

    @Override
    public void updateCacheOnDelete(Beanjection beans) {
        TaskInstance theTask = this.getTaskInstance();
        ResourceInstance theResource = this.getResourceInstance();
        WRequirement theReq = this.getRequirement();

        if (theTask != null) {
            theTask = ((TaskInstance) beans.getVariableInstanceFacade().find(theTask.getId()));
            if (theTask != null) {
                theTask.getActivities().remove(this);
            }
        }
        if (theResource != null) {
            theResource = ((ResourceInstance) beans.getVariableInstanceFacade().find(theResource.getId()));
            if (theResource != null) {
                theResource.getActivities().remove(this);
            }
        }

        if (theReq != null) {
            TaskInstance ti = theReq.getTaskInstance();
            if (ti != null) {
                ti = ((TaskInstance) beans.getVariableInstanceFacade().find(ti.getId()));
                if (ti != null) {

                    theReq = beans.getResourceFacade().findRequirement(theReq.getId());

                    //theReq = taskInstance.getRequirementById(theReq.getId());
                    if (theReq != null) {
                        theReq.removeActivity(this);
                    }
                }
            }
        }

        super.updateCacheOnDelete(beans);
    }
}
