/*
 * Wegas
 * http://wegas.albasim.ch
 *
 * Copyright (c) 2013, 2014, 2015 School of Business and Engineering Vaud, Comem
 * Licensed under the MIT License
 */
package com.wegas.reviewing.rest;

import com.wegas.core.ejb.PlayerFacade;
import com.wegas.core.ejb.RequestFacade;
import com.wegas.core.ejb.VariableDescriptorFacade;
import com.wegas.core.exception.client.WegasScriptException;
import com.wegas.core.persistence.game.Game;
import com.wegas.core.security.ejb.UserFacade;
import com.wegas.core.security.util.SecurityHelper;
import com.wegas.reviewing.ejb.ReviewingFacade;
import com.wegas.reviewing.persistence.PeerReviewInstance;
import com.wegas.reviewing.persistence.Review;
import com.wegas.reviewing.persistence.evaluation.EvaluationInstance;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.shiro.authz.UnauthorizedException;

/**
 *
 * @author Maxence Laurent (maxence.laurent gmail.com)
 */
@Stateless
@Path("GameModel/{gameModelId : [1-9][0-9]*}/VariableDescriptor/PeerReviewController/")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PeerReviewController {

    /**
     *
     */
    @EJB
    private ReviewingFacade reviewFacade;
    /**
     *
     */
    @EJB
    private RequestFacade requestFacade;
    /**
     *
     */
    @EJB
    private UserFacade userFacade;
    /**
     *
     */
    @EJB
    private PlayerFacade playerFacade;

    @EJB
    private VariableDescriptorFacade descriptorFacade;

    /**
     *
     * @param playerId
     * @param prdId
     * @return
     * @throws com.wegas.core.exception.client.WegasScriptException
     */
    @POST
    @Path("/{reviewDescriptorId : [1-9][0-9]*}/Submit/{playerId : [1-9][0-9]*}")
    public Response submit(
            @PathParam("playerId") Long playerId,
            @PathParam("reviewDescriptorId") Long prdId) throws WegasScriptException {

        checkPermissions(playerFacade.find(playerId).getGame(), playerId);

        reviewFacade.submit(prdId, playerId);

        return Response.ok().build();
    }

    @POST
    @Path("/{reviewDescriptorId : [1-9][0-9]*}/Dispatch")
    public Response dispatch(
            @PathParam("reviewDescriptorId") Long prdId
    ) {
        //checkPermissions(playerFacade.find(playerId).getGame(), playerId); // TODO Assert Trainer is the trainer !!!
        reviewFacade.dispatch(prdId);
        return Response.ok().build();
    }

    @POST
    @Path("/SaveReview")
    public PeerReviewInstance saveReview(Review other) {
        Review review = reviewFacade.findReview(other.getId());
        PeerReviewInstance instance = reviewFacade.getPeerReviewInstanceFromReview(review);
        reviewFacade.saveReview(instance, other);
        return instance;
    }

    @POST
    @Path("/SubmitReview")
    public PeerReviewInstance submitReview(Review review) {
        Review submitedReview = reviewFacade.submitReview(review);
        return reviewFacade.getPeerReviewInstanceFromReview(submitedReview);
    }

    @POST
    @Path("/{reviewDescriptorId : [1-9][0-9]*}/Notify")
    public Response notify(
            @PathParam("reviewDescriptorId") Long prdId
    ) {
        //checkPermissions(playerFacade.find(playerId).getGame(), playerId); // TODO Assert Trainer is the trainer !!!
        reviewFacade.notify(prdId);
        return Response.ok().build();
    }

    @POST
    @Path("/{reviewDescriptorId : [1-9][0-9]*}/Close")
    public Response close(
            @PathParam("reviewDescriptorId") Long prdId
    ) {
        //checkPermissions(playerFacade.find(playerId).getGame(), playerId); // TODO Assert Trainer is the trainer !!!
        reviewFacade.close(prdId);
        return Response.ok().build();
    }

    private void checkPermissions(Game game, Long playerId) throws UnauthorizedException {
        if (!SecurityHelper.isPermitted(game, "Edit") && !userFacade.matchCurrentUser(playerId)) {
            throw new UnauthorizedException();
        }
    }
}