/*
 * Wegas
 * http://wegas.albasim.ch
 *
 * Copyright (c) 2013-2018 School of Business and Engineering Vaud, Comem, MEI
 * Licensed under the MIT License
 */
package com.wegas.resourceManagement.persistence;

import com.wegas.core.exception.client.WegasIncompatibleType;
import com.wegas.core.exception.client.WegasOutOfBoundException;
import com.wegas.core.persistence.AbstractEntity;
import com.wegas.core.persistence.ListUtils;
import com.wegas.core.persistence.VariableProperty;
import com.wegas.core.persistence.variable.Beanjection;
import com.wegas.core.persistence.variable.Propertable;
import com.wegas.core.persistence.variable.VariableInstance;
import com.wegas.resourceManagement.ejb.IterationFacade;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.json.bind.annotation.JsonbProperty;
import javax.json.bind.annotation.JsonbTransient;
import javax.persistence.CascadeType;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

/**
 *
 * @author Francois-Xavier Aeberhard (fx at red-agent.com)
 */
@Entity

/*@Table(indexes = {

    @Index(columnList = "plannification.taskinstance_variableinstance_id"),
    @Index(columnList = "properties.taskinstance_variableinstance_id")
})*/
public class TaskInstance extends VariableInstance implements Propertable {

    private static final long serialVersionUID = 1L;
    /**
     *
     */
    private boolean active = true;
    /**
     *
     */
    @Transient
    private Double duration;
    /**
     *
     */
    @ElementCollection
    private List<Integer> plannification = new ArrayList<>();

    @OneToMany(mappedBy = "taskInstance", cascade = {CascadeType.ALL}, orphanRemoval = true)
    @JsonbTransient
    private List<Activity> activities = new ArrayList<>();

    @OneToMany(mappedBy = "taskInstance", cascade = {CascadeType.ALL}, orphanRemoval = true)
    @JsonbTransient
    private List<Assignment> assignments = new ArrayList<>();

    @ManyToMany(mappedBy = "tasks")
    //@WegasJsonView(Views.ExtendedI.class)
    @JsonbTransient
    private List<Iteration> iterations;

    /**
     *
     */
    @ElementCollection
    @JsonbTransient
    private List<VariableProperty> properties = new ArrayList<>();
    /**
     *
     */
    @OneToMany(mappedBy = "taskInstance", cascade = {CascadeType.ALL}, orphanRemoval = true)
    //@JoinColumn(referencedColumnName = "variableinstance_id")
    private List<WRequirement> requirements = new ArrayList<>();

    /**
     * @return the active
     */
    public boolean getActive() {
        return this.active;
    }

    /**
     * @param active the active to set
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * @return the duration
     */
    @JsonbTransient
    public Double getDuration() {
        return duration;
    }

    /**
     * @deprecated moved as instance property, setter kept for old JSON backward
     * compatibility
     * @param duration the duration to set
     */
    @JsonbProperty
    public void setDuration(double duration) {
        if (duration < 0.0) {
            throw new WegasOutOfBoundException(0.0, null, duration, "duration", "duration");
        } else {
            this.duration = duration;
        }
    }

    @JsonbTransient
    @Override
    public List<VariableProperty> getInternalProperties() {
        return this.properties;
    }

    /**
     * @return the plannification
     */
    public List<Integer> getPlannification() {
        return plannification;
    }

    /**
     * @param plannification the plannification to set
     */
    public void setPlannification(List<Integer> plannification) {
        this.plannification = plannification;
    }

    /**
     * @return the activities
     */
    public List<Activity> getActivities() {
        return activities;
    }

    /**
     * @param activities
     */
    public void setActivities(List<Activity> activities) {
        this.activities = activities;
    }

    /**
     *
     * @param activity
     */
    public void addActivity(Activity activity) {
        this.activities.add(activity);
        activity.setTaskInstance(this);
    }

    /**
     *
     * @param activity
     */
    public void removeActivity(Activity activity) {
        this.activities.remove(activity);
    }

    /**
     * @return the assignments
     */
    public List<Assignment> getAssignments() {
        return assignments;
    }

    /**
     * @param assignments
     */
    public void setAssignments(List<Assignment> assignments) {
        this.assignments = assignments;
    }

    /**
     *
     * @param assignment
     */
    public void addAssignment(Assignment assignment) {
        assignments.add(assignment);
        assignment.setTaskInstance(this);
    }

    public void removeAssignment(Assignment assignment) {
        assignments.remove(assignment);
    }

    /**
     *
     * @return get all iterations this task is part of
     */
    @JsonbTransient
    public List<Iteration> getIterations() {
        return iterations;
    }

    /**
     *
     * @param iterations
     */
    @JsonbTransient
    public void setIterations(List<Iteration> iterations) {
        this.iterations = iterations;
    }

    /**
     *
     * @param index
     *
     * @return WRequirement
     */
    public WRequirement getRequirement(Integer index) {
        return this.requirements.get(index);
    }

    /**
     *
     * @param id
     *
     * @return requirement matching given id
     */
    public WRequirement getRequirementById(Long id) {
        WRequirement requirement = null;
        for (WRequirement req : this.getRequirements()) {
            if (Objects.equals(req.getId(), id)) {
                requirement = req;
                break;
            }
        }
        return requirement;
    }

    public WRequirement getRequirementByName(String name) {
        WRequirement requirement = null;
        for (WRequirement req : this.getRequirements()) {
            if (req.getName().equals(name)) {
                requirement = req;
                break;
            }
        }
        return requirement;
    }

    /**
     * @return the requirements
     */
    public List<WRequirement> getRequirements() {
        return this.requirements;
    }

    /**
     * @param requirements the requirement to set
     */
    public void setRequirements(List<WRequirement> requirements) {
        this.requirements = requirements;
        for (WRequirement req : requirements) {
            req.setTaskInstance(this);
        }
    }

    /**
     *
     * @param index
     * @param val
     */
    public void setRequirement(Integer index, WRequirement val) {
        this.getRequirements().set(index, val);
        val.setTaskInstance(this);
    }

    public void addRequirement(WRequirement req) {
        this.getRequirements().add(req);
        req.setTaskInstance(this);
    }

    /**
     *
     * @param a
     */
    @Override
    public void merge(AbstractEntity a) {
        if (a instanceof TaskInstance) {
            super.merge(a);
            TaskInstance other = (TaskInstance) a;
            this.setActive(other.getActive());
            //this.setDuration(other.getDuration());
            this.setProperties(other.getProperties());
            ListUtils.KeyExtractorI<Object, WRequirement> converter;
            converter = new WRequirementToNameConverter();

            this.setRequirements(ListUtils.mergeLists(this.getRequirements(), other.getRequirements(), converter));

            /*
            Map<String, WRequirement> reqMap = ListUtils.listAsMap(requirements, converter);
            this.setRequirements(new ArrayList<>());
            for (WRequirement req : other.getRequirements()) {
                WRequirement r;
                if (reqMap.containsKey(req.getName()) && req.getId() != null) {
                    r = reqMap.get(req.getName());
                    r.merge(req);
                    this.getRequirements().add(r);
                } else {
                    r = new WRequirement();
                    r.merge(req);
                    r.setTaskInstance(this);
                    this.getRequirements().add(r);
                }
            }*/
            this.setPlannification(new ArrayList<>());
            this.getPlannification().addAll(other.getPlannification());
        } else {
            throw new WegasIncompatibleType(this.getClass().getSimpleName() + ".merge (" + a.getClass().getSimpleName() + ") is not possible");
        }
    }

    private static class WRequirementToNameConverter implements ListUtils.KeyExtractorI<Object, WRequirement> {

        @Override
        public String getKey(WRequirement item) {
            return item.getName();
        }
    }

    @Override
    public void updateCacheOnDelete(Beanjection beans) {
        IterationFacade iteF = beans.getIterationFacade();

        for (Iteration iteration : this.getIterations()) {
            iteration = iteF.find(iteration.getId());
            if (iteration != null) {
                iteration.removeTask(this);
            }
        }
        this.setIterations(new ArrayList<>());

        super.updateCacheOnDelete(beans);
    }
}
