/*
 * Wegas
 * http://wegas.albasim.ch
 *
 * Copyright (c) 2013, 2014, 2015 School of Business and Engineering Vaud, Comem
 * Licensed under the MIT License
 */
package com.wegas.core.rest;

import com.wegas.core.ejb.GameFacade;
import com.wegas.core.ejb.PlayerFacade;
import com.wegas.core.ejb.RequestManager;
import com.wegas.core.ejb.TeamFacade;
import com.wegas.core.exception.client.WegasNotFoundException;
import com.wegas.core.persistence.game.DebugTeam;
import com.wegas.core.persistence.game.Game;
import com.wegas.core.persistence.game.Player;
import com.wegas.core.persistence.game.Team;
import com.wegas.core.security.ejb.UserFacade;
import com.wegas.core.security.persistence.User;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collection;

/**
 * @author Francois-Xavier Aeberhard (fx at red-agent.com)
 */
@Stateless
@Path("GameModel/{gameModelId : ([1-9][0-9]*)?}{sep: /?}Game/{gameId : ([1-9][0-9]*)?}{sep2: /?}Team/{teamId : [1-9][0-9]*}/Player")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PlayerController {

    /**
     *
     */
    @EJB
    private UserFacade userFacade;
    @Inject
    private RequestManager requestManager;
    /**
     *
     */
    @EJB
    private PlayerFacade playerFacade;
    /**
     *
     */
    @EJB
    private TeamFacade teamFacade;
    /**
     *
     */
    @EJB
    private GameFacade gameFacade;

    /**
     * @param playerId
     *
     * @return the player matching given id
     */
    @GET
    @Path("{playerId : [1-9][0-9]*}")
    public Player get(@PathParam("playerId") Long playerId) {
        Player p = playerFacade.find(playerId);
        return playerFacade.find(playerId);
    }

    /**
     * @param teamId
     *
     * @return HTTP 201 with the team or 4xx if something went wrong
     */
    @POST
    public Response create(@PathParam("teamId") Long teamId) {
        User currentUser;
        try {
            currentUser = userFacade.getCurrentUser();
        } catch (WegasNotFoundException ex) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        Team teamToJoin = teamFacade.find(teamId);
        if (teamToJoin != null) {
            if (!(teamToJoin instanceof DebugTeam)
                    && teamToJoin.getGame().getAccess() == Game.GameAccess.OPEN
                    && !teamToJoin.getGame().getProperties().getFreeForAll()) {
                if (requestManager.tryLock("join-" + teamToJoin.getGameId() + "-" + currentUser.getId())) {

                    if (!playerFacade.isInGame(teamToJoin.getGameId(), currentUser.getId())) {
                        gameFacade.joinTeam(teamToJoin.getId(), currentUser.getId());
                        // reload up to date team
                        teamFacade.detach(teamToJoin);
                        teamToJoin = teamFacade.find(teamToJoin.getId());
                        return Response.status(Response.Status.CREATED).entity(teamToJoin).build();
                    }
                }
                return Response.status(Response.Status.CONFLICT).build();
            }
            //Not a joinable team (debugteam, closed game or individual game) or user already registerd within the game
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        //the team doesn't exists
        return Response.status(Response.Status.BAD_REQUEST).build();
    }

    /**
     * Update a player
     *
     * @param playerId id of player to update
     * @param entity
     *
     * @return up to date player
     */
    @PUT
    @Path("{playerId: [1-9][0-9]*}")
    public Player update(@PathParam("playerId") Long playerId, Player entity) {
        return playerFacade.update(playerId, entity);
    }

    /**
     * @param playerId
     *
     * @return just deleted player
     */
    @DELETE
    @Path("{playerId: [1-9][0-9]*}")
    public Player delete(@PathParam("playerId") Long playerId) {
        Player p = playerFacade.find(playerId);
        playerFacade.remove(p);
        return p;
    }

    /**
     * Resets all the variables of a given player
     *
     * @param playerId playerId
     *
     * @return HTTP 200 OK
     */
    @GET
    @Path("{playerId : [1-9][0-9]*}/Reset")
    public Response reset(@PathParam("playerId") Long playerId) {
        Player p = playerFacade.find(playerId);

        playerFacade.reset(p);
        return Response.ok().build();
    }

    @GET
    @Path("{playerId : [1-9][0-9]*}/Locks")
    public Collection<String> getLocks(@PathParam("playerId") Long playerId) {
        return playerFacade.getLocks(playerId);
    }
}
