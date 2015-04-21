/*
 * Wegas
 * http://wegas.albasim.ch
 *
 * Copyright (c) 2013, 2014, 2015 School of Business and Engineering Vaud, Comem
 * Licensed under the MIT License
 */
package com.wegas.reviewing.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import com.wegas.reviewing.persistence.evaluation.EvaluationDescriptor;
import com.wegas.core.persistence.AbstractEntity;
import com.wegas.core.persistence.variable.VariableDescriptor;
import com.wegas.core.rest.util.Views;
import com.wegas.reviewing.persistence.evaluation.EvaluationDescriptorContainer;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * PeerReviewDescriptor allows peer-reviewing of variable between
 * (scope-dependent) Player/Team (ie the "author" and the "reviewers").
 *
 * A review: <ul>
 * <li> is made for a specific variable ('toReview' VariableDescriptor)</li>
 * <li> is define as, at least, one evaluation, defined as a 'feedback', wrapped
 * within a container</li>
 * <li> is done by several players/teams (reviewers) (up to
 * 'maxNumberOfReviewer'). Each author is reviewed the given number of times and
 * is a 'reviewer' for the same number of others authors</li>
 * </ul>
 *
 * Moreover, feedbacks can be evaluated by the author. Such an evaluation is
 * define within an EvaluationDescriptorContainer('feedbacksEvaluation', nested
 * list can be empty)
 *
 * The reviewing process consists of X stage:
 * <ol>
 * <li> not-started: author edit its 'toReview' variable instance</li>
 * <li> submitted: 'toReview' instances no longer editable </li>
 * <li> dispatched: variable turns read-only, reviewers are chosen by such an
 * algorithm</li>
 * </ol>
 *
 * @author Maxence Laurent (maxence.laurent gmail.com)
 * @see EvaluationDescriptor
 * @see PeerReviewInstance
 */
@Entity
public class PeerReviewDescriptor extends VariableDescriptor<PeerReviewInstance> {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(PeerReviewDescriptor.class);

    /**
     * Define review states
     */
    public enum ReviewingState {
        NOT_STARTED, // author can edit toReview
        SUBMITTED,   // authors can't edit toReview anymore
        DISPATCHED,   // toReview are dispatched, state became review dependent
        DISCARDED   // completely out of reviewing process (debug team for instance)
    }

    /**
     * the variable to review
     */
    @ManyToOne
    @JsonIgnore
    private VariableDescriptor toReview;

    /**
     * the name of the variable to review. Only used for JSON de serialisation
     */
    @Transient
    private String toReviewName;

    /**
     * Expected number of reviews. The number of reviews may be smaller,
     * especially is total number of team/player is too small
     *
     */
    private Integer maxNumberOfReviewer;

    @Basic(fetch = FetchType.LAZY)
    @Lob
    private String description;

    /**
     * List of evaluations that compose one feedback. Here, en empty list does
     * not make any sense
     */
    @OneToOne(cascade = CascadeType.ALL)
    @JsonView(Views.EditorI.class)
    @NotNull
    private EvaluationDescriptorContainer feedback;

    /**
     * List of evaluations that compose the feedbacks evaluations. Empty list is
     * allowed
     */
    @OneToOne(cascade = CascadeType.ALL)
    @JsonView(Views.EditorI.class)
    @NotNull
    private EvaluationDescriptorContainer feedbackEvaluation;

    /**
     *
     */
    public PeerReviewDescriptor() {
        super();
    }

    /**
     *
     * @param name variable unique name
     */
    public PeerReviewDescriptor(String name) {
        super(name);
    }

    /**
     *
     * @param name            variable unique name
     * @param defaultInstance
     */
    public PeerReviewDescriptor(String name, PeerReviewInstance defaultInstance) {
        super(name, defaultInstance);
    }

    /**
     *
     * @param a another PeerReviewDescriptor
     */
    @Override
    public void merge(AbstractEntity a) {
        if (a instanceof PeerReviewDescriptor) {
            PeerReviewDescriptor other = (PeerReviewDescriptor) a;
            super.merge(a);

            this.setMaxNumberOfReview(other.getMaxNumberOfReview());
            this.setDescription(other.getDescription());
            this.setToReview(other.getToReview());
            this.setToReviewName(other.getToReviewName());
            this.feedback.merge(other.getFeedback());
            this.feedbackEvaluation.merge(other.getFeedbackEvaluation());
        }
    }

    /**
     * Return the variable that will be reviewed
     *
     * @return
     */
    public VariableDescriptor getToReview() {
        return toReview;
    }

    /**
     * Set the variable to review
     *
     * @param toReview
     */
    public void setToReview(VariableDescriptor toReview) {
        this.toReview = toReview;
    }

    /**
     * get the name of the variable to review
     *
     * @return variable to review unique name
     */
    public String getToReviewName() {
        return (toReview != null ? toReview.getName() : this.toReviewName);
    }

    /**
     * Used to fetch the JSON de-serialised variable name
     *
     * @return
     */
    @JsonIgnore
    public String getImportedToReviewName() {
        return toReviewName;
    }

    /**
     * set the name of the variable to review
     *
     * @param toReviewName the name to review
     */
    public void setToReviewName(String toReviewName) {
        this.toReviewName = toReviewName;
    }

    /**
     * get the expected number of reviewers
     *
     * @return expected number of reviewers
     */
    public Integer getMaxNumberOfReview() {
        return maxNumberOfReviewer;
    }

    /**
     * set the expected number of reviewers
     *
     * @param maxNumberOfReviewer the number of expected reviewers, shall be > 0
     *
     */
    public void setMaxNumberOfReview(Integer maxNumberOfReviewer) {
        if (maxNumberOfReviewer >= 0) {
            this.maxNumberOfReviewer = maxNumberOfReviewer;
        } else {
            this.maxNumberOfReviewer = 1; // TODO throw error ? 
        }
    }


    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }


    /**
     * get the feedback description
     *
     * @return a list of EvaluationDescriptor
     */
    public EvaluationDescriptorContainer getFeedback() {
        return feedback;
    }

    /**
     * set the feedback description
     *
     * @param feedback s list of EvaluationDescriptor
     */
    public void setFeedback(EvaluationDescriptorContainer feedback) {
        this.feedback = feedback;
        if (feedback != null) {
            //feedback.setParent(this);
        }
    }

    /**
     * get the feedback evaluation description
     *
     * @return list of EvaluationDescriptor
     */
    public EvaluationDescriptorContainer getFeedbackEvaluation() {
        return feedbackEvaluation;
    }

    /**
     *
     * set the feedback evaluation description
     *
     * @param feedbackEvaluation list of evaluation descriptor
     */
    public void setFeedbacksEvaluation(EvaluationDescriptorContainer feedbackEvaluation) {
        this.feedbackEvaluation = feedbackEvaluation;
        if (feedbackEvaluation != null) {
            //feedbackEvaluation.setParent(this);
        }
    }

}
