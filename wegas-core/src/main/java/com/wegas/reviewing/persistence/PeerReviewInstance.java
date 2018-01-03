/*
 * Wegas
 * http://wegas.albasim.ch
 *
 * Copyright (c) 2013-2018 School of Business and Engineering Vaud, Comem, MEI
 * Licensed under the MIT License
 */
package com.wegas.reviewing.persistence;

import com.wegas.core.merge.annotations.WegasEntityProperty;
import com.wegas.core.persistence.variable.Beanjection;
import com.wegas.core.persistence.variable.VariableInstance;
import com.wegas.core.security.util.WegasPermission;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.OneToMany;

/**
 * Instance of the PeerReviewDescriptor variable Author:<br />
 * - has to review several other authors: <code>toReview</code> Review
 * list<br />
 * - is reviewed by several other authors: <code>reviewed</code> Review list The
 * review is in a specific state, see PeerReviewDescriptor
 *
 * @author Maxence Laurent (maxence.laurent gmail.com)
 * @see PeerReviewDescriptor
 */
@Entity
public class PeerReviewInstance extends VariableInstance {

    private static final long serialVersionUID = 1L;

    /**
     * Current review state
     */
    @Enumerated(value = EnumType.STRING)
    @WegasEntityProperty
    private PeerReviewDescriptor.ReviewingState reviewState = PeerReviewDescriptor.ReviewingState.NOT_STARTED;

    /**
     * List of review that contains feedback written by player owning this
     */
    @OneToMany(mappedBy = "reviewer", cascade = CascadeType.ALL, orphanRemoval = true)
    @WegasEntityProperty
    private List<Review> toReview = new ArrayList<>();

    /**
     * List of review that contains others feedback
     */
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
    @WegasEntityProperty
    private List<Review> reviewed = new ArrayList<>();

    /**
     * Get the current state of the review
     *
     * @return the current state
     */
    public PeerReviewDescriptor.ReviewingState getReviewState() {
        return reviewState;
    }

    /**
     * Set the current state of the review
     *
     * @param state the new current state
     */
    public void setReviewState(PeerReviewDescriptor.ReviewingState state) {
        this.reviewState = state;
    }

    /**
     * Get the list of feedback to write to review others authors
     *
     * @return the list of feedback
     */
    public List<Review> getToReview() {
        return this.toReview;
    }

    /**
     * Set the list of feedback to write to review others authors
     *
     * @param toReview list of Review
     */
    public void setToReview(List<Review> toReview) {
        this.toReview = toReview;
        for (Review review : toReview){
            review.setReviewer(this);
        }
    }

    /**
     * @param r
     */
    public void addToToReview(Review r) {
        this.getToReview().add(r);
        r.setReviewer(this);
    }

    /**
     * Get the list of feedback written by others authors
     *
     * @return all feedbacks from others
     */
    public List<Review> getReviewed() {
        return this.reviewed;
    }

    /**
     * Set the list of feedback written by others authors
     *
     * @param reviewed
     */
    public void setReviewed(List<Review> reviewed) {
        this.reviewed = reviewed;

        for (Review review : reviewed){
            review.setAuthor(this);
        }
    }

    public void addToReviewed(Review r) {
        this.getReviewed().add(r);
        r.setAuthor(this);
    }

    @Override
    public void revive(Beanjection beans) {
        beans.getReviewingFacade().revivePeerReviewInstance(this);
    }

    /**
     * Skip this {@link #getRequieredReadPermission() } implementation.
     * call super one.
     */
    private Collection<WegasPermission> super_getRequieredReadPermission() {
        return super.getRequieredReadPermission();
    }

    @Override
    public Collection<WegasPermission> getRequieredReadPermission() {
        Collection<WegasPermission> ps = super.getRequieredReadPermission();
        // reviewer also have right to read
        for (Review r : getReviewed()) {
            ps.addAll(r.getReviewer().super_getRequieredReadPermission()); // avoid infinite loop
        }
        // so authors have
        for (Review r : getToReview()) {
            ps.addAll(r.getAuthor().super_getRequieredReadPermission()); // avoid infinite loop
        }
        return ps;
    }

    /**
     * Skip this {@link #getRequieredUpdatePermission() } implementation.
     * call super one.
     */
    private Collection<WegasPermission> super_getRequieredUpdatePermission() {
        return super.getRequieredUpdatePermission();
    }

    @Override
    public Collection<WegasPermission> getRequieredUpdatePermission() {
        Collection<WegasPermission> ps = super.getRequieredUpdatePermission();
        for (Review r : getReviewed()) {
            // when they'er reviewing, reviewers also have right to write (optmisticlock = cascade on variableinstance !)
            if (r.getInitialReviewState().equals(Review.ReviewState.DISPATCHED)) {
                ps.addAll(r.getReviewer().super_getRequieredUpdatePermission()); // avoid infinite loop
            }
        }
        for (Review r : getToReview()) {
            // when they'er commenting the feedback, authors also have right to write (optmisticlock = cascade on variableinstance !)
            if (r.getInitialReviewState().equals(Review.ReviewState.NOTIFIED)) {
                ps.addAll(r.getAuthor().super_getRequieredUpdatePermission()); // avoid infinite loop
            }
        }
        return ps;
    }
}
