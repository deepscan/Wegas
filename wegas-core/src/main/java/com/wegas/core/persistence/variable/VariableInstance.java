/*
 * Wegas
 * http://wegas.albasim.ch
 *
 * Copyright (c) 2013-2018 School of Business and Engineering Vaud, Comem, MEI
 * Licensed under the MIT License
 */
package com.wegas.core.persistence.variable;

import com.wegas.core.persistence.AbstractEntity;
import com.wegas.core.persistence.Broadcastable;
import com.wegas.core.persistence.InstanceOwner;
import com.wegas.core.persistence.game.Game;
import com.wegas.core.persistence.game.GameModel;
import com.wegas.core.persistence.game.Player;
import com.wegas.core.persistence.game.Team;
import com.wegas.core.persistence.variable.scope.*;
import com.wegas.core.persistence.views.Views;
import com.wegas.core.persistence.views.WegasJsonView;
import com.wegas.core.security.util.WegasPermission;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.json.bind.annotation.JsonbTransient;
import javax.persistence.*;
import org.eclipse.persistence.annotations.CacheIndex;
import org.eclipse.persistence.annotations.CacheIndexes;
import org.eclipse.persistence.annotations.OptimisticLocking;
import org.eclipse.persistence.config.CacheUsage;
import org.eclipse.persistence.config.QueryHints;
import org.eclipse.persistence.config.QueryType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Francois-Xavier Aeberhard (fx at red-agent.com)
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@NamedQueries({
    @NamedQuery(name = "VariableInstance.findPlayerInstance",
            query = "SELECT vi FROM VariableInstance vi WHERE "
            + "(vi.player.id = :playerId AND vi.playerScope.id = :scopeId)",
            hints = {
                @QueryHint(name = QueryHints.QUERY_TYPE, value = QueryType.ReadObject),
                @QueryHint(name = QueryHints.CACHE_USAGE, value = CacheUsage.CheckCacheThenDatabase)
            }
    ),
    @NamedQuery(name = "VariableInstance.findTeamInstance",
            query = "SELECT vi FROM VariableInstance vi WHERE "
            + "(vi.team.id = :teamId AND vi.teamScope.id = :scopeId)",
            hints = {
                @QueryHint(name = QueryHints.QUERY_TYPE, value = QueryType.ReadObject),
                @QueryHint(name = QueryHints.CACHE_USAGE, value = CacheUsage.CheckCacheThenDatabase)
            }
    ),
    @NamedQuery(name = "VariableInstance.findGameInstance",
            query = "SELECT vi FROM VariableInstance vi WHERE "
            + "(vi.game.id = :gameId AND vi.gameScope.id = :scopeId)",
            hints = {
                @QueryHint(name = QueryHints.QUERY_TYPE, value = QueryType.ReadObject),
                @QueryHint(name = QueryHints.CACHE_USAGE, value = CacheUsage.CheckCacheThenDatabase)
            }
    ),
    @NamedQuery(name = "VariableInstance.findAllPlayerInstances",
            query = "SELECT vi FROM VariableInstance vi WHERE "
            + "(vi.playerScope.id = :scopeId)"
    ),
    @NamedQuery(name = "VariableInstance.findAllTeamInstances",
            query = "SELECT vi FROM VariableInstance vi WHERE "
            + "(vi.teamScope.id = :scopeId)"
    ),
    @NamedQuery(name = "VariableInstance.findAllGameInstances",
            query = "SELECT vi FROM VariableInstance vi WHERE "
            + "(vi.gameScope.id = :scopeId)"
    )
})
@CacheIndexes(value = {
    @CacheIndex(columnNames = {"GAMESCOPE_ID", "GAMEVARIABLEINSTANCES_KEY"}),
    @CacheIndex(columnNames = {"TEAMSCOPE_ID", "TEAMVARIABLEINSTANCES_KEY"}),
    @CacheIndex(columnNames = {"PLAYERSCOPE_ID", "VARIABLEINSTANCES_KEY"})
})
/*@Indexes(value = { // JPA 2.0 eclipse link extension TO BE REMOVED

 @Index(name = "index_variableinstance_gamescope_id", columnNames = {"gamescope_id"}),
 @Index(name = "index_variableinstance_teamscope_id", columnNames = {"teamscope_id"}),
 @Index(name = "index_variableinstance_playerscope_id", columnNames = {"playerscope_id"})
 })*/
/* JPA2.1 (GlassFish4) Indexes */
@Table(indexes = {
    @Index(columnList = "gamescope_id"),
    @Index(columnList = "teamscope_id"),
    @Index(columnList = "playerscope_id"),
    @Index(columnList = "variableinstances_key"),
    @Index(columnList = "teamvariableinstances_key"),
    @Index(columnList = "gamevariableinstances_key"),
    @Index(columnList = "gamemodelvariableinstances_key")
})
//@JsonIgnoreProperties(value={"descriptorId"})
@OptimisticLocking(cascade = true)
//@Cacheable(false)
abstract public class VariableInstance extends AbstractEntity implements Broadcastable {

    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(VariableInstance.class);

    @Version
    @Column(columnDefinition = "bigint default '0'::bigint")
    private Long version;

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    /**
     *
     */
    @Id
    @Column(name = "variableinstance_id")
    @GeneratedValue
    @WegasJsonView(Views.IndexI.class)
    private Long id;

    /**
     *
     */
    @ManyToOne
    @JsonbTransient
    @JoinColumn(name = "gamescope_id")
    private GameScope gameScope;

    /**
     *
     */
    @ManyToOne
    @JsonbTransient
    @JoinColumn(name = "teamscope_id")
    private TeamScope teamScope;

    /**
     *
     */
    @ManyToOne
    @JsonbTransient
    @JoinColumn(name = "playerscope_id")
    private PlayerScope playerScope;

    /**
     *
     */
    @OneToOne(fetch = FetchType.LAZY, mappedBy = "variableInstance")
    @JsonbTransient
    private GameModelScope gameModelScope;
    /**
     *
     */
    @OneToOne(fetch = FetchType.LAZY, mappedBy = "defaultInstance")
    @JsonbTransient
    private VariableDescriptor defaultDescriptor;

    /**
     *
     * @Column(name = "variableinstances_key", insertable = false, updatable =
     * false, columnDefinition = "bigint") private Long playerScopeKey;
     */
    /**
     *
     */
    @JoinColumn(name = "variableinstances_key")
    @ManyToOne
    @JsonbTransient
    private Player player;
    /**
     *
     * @Column(name = "gamevariableinstances_key", insertable = false, updatable
     * = false, columnDefinition = "bigint") private Long gameScopeKey;
     */
    /**
     *
     */
    @JoinColumn(name = "gamevariableinstances_key")
    @ManyToOne
    @JsonbTransient
    private Game game;

    @JoinColumn(name = "gamemodelvariableinstances_key")
    @ManyToOne
    @JsonbTransient
    private GameModel gameModel;

    /**
     *
     * @Column(name = "teamvariableinstances_key", insertable = false, updatable
     * = false, columnDefinition = "bigint") private Long teamScopeKey;
     */
    /**
     *
     */
    @JoinColumn(name = "teamvariableinstances_key")
    @ManyToOne
    @JsonbTransient
    private Team team;

    @Override
    public VariableInstance clone() {
        return (VariableInstance) super.clone();
    }

    /**
     * Return the effective owner of the instance, null for default instances
     *
     * @return effective instance owner, but null for default ones
     */
    @JsonbTransient
    public InstanceOwner getOwner() {
        if (isDefaultInstance()) {
            return null;
        } else {
            if (this.getTeam() != null) {
                return this.getTeam();
            } else if (this.getPlayer() != null) {
                return this.getPlayer();
            } else if (this.getGame() != null) {
                return this.getGame();
            } else {
                return this.getGameModel();
                //return this.findDescriptor().getGameModel();
            }
        }
    }

    /**
     *
     * Same as getOwner but return the gameModel for default instances
     *
     * @return effective instance owner or the gameModel for default ones
     */
    @JsonbTransient
    public InstanceOwner getEffectiveOwner() {
        if (this.getTeam() != null) {
            return this.getTeam();
        } else if (this.getPlayer() != null) {
            return this.getPlayer();
        } else if (this.getGame() != null) {
            return this.getGame();
        } else {
            //return this.getGameModel();
            return this.findDescriptor().getGameModel();
        }
    }

    /**
     * Get the channel to broadcast this instance on, according to scope and broadcast scope
     *
     * @return
     */
    @JsonbTransient
    public String getAudience() {
        InstanceOwner audienceOwner = getBroadcastTarget();
        if (audienceOwner != null) {
            return audienceOwner.getChannel();
        } else {
            return null;
        }
    }

    @JsonbTransient
    public InstanceOwner getBroadcastTarget() {
        if (this.getTeam() != null) {
            if (this.getTeamScope().getBroadcastScope().equals("GameScope")) {
                return this.getTeam().getGame();
            } else {
                return this.getTeam();
            }
        } else if (this.getPlayer() != null) {
            if (this.getPlayerScope().getBroadcastScope().equals("TeamScope")) {

                return this.getPlayer().getTeam();
            } else if (this.getPlayerScope().getBroadcastScope().equals("GameScope")) {
                return this.getPlayer().getGame();
            } else {
                return this.getPlayer();
            }
        } else if (this.getGame() != null) {
            return this.getGame();
        } else if (this.gameModelScope != null) {
            // this.getGameModel().getChannel();
            return this.getGameModelScope().getVariableDescriptor().getGameModel();
        } else {
            // Default instance
            return null;
        }
    }

    @Override
    public Map<String, List<AbstractEntity>> getEntities() {
        String audience = this.getAudience();
        if (audience != null) {
            Map<String, List<AbstractEntity>> map = new HashMap<>();
            ArrayList<AbstractEntity> entities = new ArrayList<>();
            entities.add(this);
            map.put(this.getAudience(), entities);
            return map;
        } else if (this.getDefaultDescriptor() != null) {
            // Default instance -> Propagate descriptor
            return this.getDefaultDescriptor().getEntities();
        } else {
            return null;
        }
    }

    /*
      @PostUpdate @PostRemove @PostPersist public void
      onInstanceUpdate() { // If the instance has no scope, it means it's a
      default if (this.getScope() != null) { //
      RequestFacade.lookup().getRequestManager().addUpdatedInstance(this); } }
     */
    /**
     * @return the scope
     */
    @JsonbTransient
    public AbstractScope getScope() {
        if (this.getTeamScope() != null) {
            return this.getTeamScope();
        } else if (this.getPlayerScope() != null) {
            return this.getPlayerScope();
        } else if (this.getGameScope() != null) {
            return this.getGameScope();
        } else if (this.getGameModelScope() != null) {
            return this.getGameModelScope();
        } else {
            return null;
        }
    }

    public Long getScopeKey() {
        if (this.getTeamScope() != null) {
            return this.getTeam().getId();
        } else if (this.getPlayerScope() != null) {
            return this.getPlayer().getId();
        } else if (this.getGameScope() != null) {
            return this.getGame().getId();
        } else if (this.getGameModelScope() != null) {
            return 0l; // hack -> see datasource instance cache mechanism
        } else {
            return null;
        }
    }

    /**
     *
     * @param key
     */
    public void setScopeKey(Long key) {
        // Just to be ignored
    }

    /**
     * Get instance descriptor through its scope.
     *
     * @return the descriptor or null if this is a default instance
     */
    @JsonbTransient
    public VariableDescriptor getDescriptor() {
        if (this.isDefaultInstance()) {
            return null;
        } else {
            return this.getScope().getVariableDescriptor();
        }
    }

    /**
     * Get instance descriptor's id through its scope for regular instance or
     * the default descriptor's id for default instances
     *
     * @return descriptor id
     */
    @WegasJsonView(Views.IndexI.class)
    public Long getDescriptorId() {
        return this.findDescriptor().getId();
    }

    /**
     * Dummy so that jaxb doesnt yell
     *
     * @param l
     */
    public void setDescriptorId(Long l) {
        // Dummy so that jaxb doesnt yell
    }

    /**
     * @return player if any
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * @return game if any
     */
    public Game getGame() {
        return game;
    }

    public GameModel getGameModel() {
        return gameModel;
    }

    public void setGameModel(GameModel gameModel) {
        this.gameModel = gameModel;
    }

    /**
     * @return team if any
     */
    public Team getTeam() {
        return team;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    /**
     * @return the id
     */
    @Override
    public Long getId() {
        return id;
    }

    /*public void setId(Long id) {
        //Thread.dumpStack();
        this.id = id;
    }*/
    /**
     * Id of the team owning the instance
     *
     * @return team's id or null if instance is not a team instance
     *
     * @JsonbTransient public Long getTeamScopeKey() { return teamScopeKey; }
     */
    /**
     * @param teamScopeKey public void setTeamScopeKey(Long teamScopeKey) {
     *                     this.teamScopeKey = teamScopeKey; }
     */
    /**
     * Id of player owning the instance
     *
     * @return player's id or null if this is not a player instance
     *
     * @JsonbTransient public Long getPlayerScopeKey() { return playerScopeKey; }
     */
    /**
     *
     * @return the gameScope or null if this instance doesn't belong to a game
     */
    @JsonbTransient
    public GameScope getGameScope() {
        return gameScope;
    }

    /**
     * @param playerScopeKey public void setPlayerScopeKey(Long playerScopeKey)
     *                       { this.playerScopeKey = playerScopeKey; }
     */
    /**
     * @param gameScopeKey public void setGameScopeKey(Long gameScopeKey) {
     *                     this.gameScopeKey = gameScopeKey; }
     */
    /**
     * @param gameScope the gameScope to set
     */
    @JsonbTransient
    public void setGameScope(GameScope gameScope) {
        this.gameScope = gameScope;
    }

    /**
     * @return the team or null if this instance doesn't belong to a team
     *         (belonging to the game for instance)
     */
    @JsonbTransient
    public TeamScope getTeamScope() {
        return teamScope;
    }

    /**
     * @param teamScope the teamScope to set
     */
    @JsonbTransient
    public void setTeamScope(TeamScope teamScope) {
        this.teamScope = teamScope;
    }

    /**
     * @return the playerScope
     */
    @JsonbTransient
    public PlayerScope getPlayerScope() {
        return playerScope;
    }

    /**
     * @param playerScope the playerScope to set
     */
    @JsonbTransient
    public void setPlayerScope(PlayerScope playerScope) {
        this.playerScope = playerScope;
    }

    /**
     * @return the defaultDescriptor
     */
    public VariableDescriptor getDefaultDescriptor() {
        return defaultDescriptor;
    }

    @JsonbTransient
    public boolean isDefaultInstance() {
        //instance without scope meads default instance
        return this.getScope() == null;
    }

    /**
     * return instance descriptor equals the instance is a default or effective
     * one
     *
     * @return instance descriptor
     */
    public VariableDescriptor findDescriptor() {
        if (this.isDefaultInstance()) {
            return this.getDefaultDescriptor();
        } else {
            return this.getDescriptor();
        }
    }

    /**
     * @param defaultDescriptor the defaultDescriptor to set
     */
    public void setDefaultDescriptor(VariableDescriptor defaultDescriptor) {
        this.defaultDescriptor = defaultDescriptor;
    }

    /**
     * @return the gameModelScope of instance id gameModel scoped, null
     *         otherwise
     */
    public GameModelScope getGameModelScope() {
        return gameModelScope;
    }

    /**
     * @param gameModelScope the gameModelScope to set
     */
    public void setGameModelScope(GameModelScope gameModelScope) {
        this.gameModelScope = gameModelScope;
    }

    @Override
    public void merge(AbstractEntity other) {
        if (other instanceof VariableInstance) {
            VariableInstance instance = (VariableInstance) other;
            this.setVersion(instance.getVersion());
        }
    }

    public void revive(Beanjection beans) {
    }

    /**
     *
     * @return string representation of the instance (class name, id, default or
     *         not, ...)
     */
    @Override
    public String toString() {
        if (this.getDefaultDescriptor() != null) {
            return "Default " + this.getClass().getSimpleName() + "( " + getId() + ") for " + this.getDefaultDescriptor().getName();
        } else if (this.getDescriptor() != null) {
            return this.getClass().getSimpleName() + "( " + getId() + ") for " + this.getDescriptor().getName();
        } else {
            return this.getClass().getSimpleName() + "( " + getId() + ") NO DESC";
        }
    }

    @Override
    public Collection<WegasPermission> getRequieredUpdatePermission() {
        if (this.getScope() == null) {
            // Default Instance is only editable with edit permission on Descriptor
            return this.getDefaultDescriptor().getRequieredUpdatePermission();
        } else {
            if (this.getTeamScope() != null || this.getPlayerScope() != null) {
                return WegasPermission.getAsCollection(this.getEffectiveOwner().getAssociatedWritePermission());
            } else if (this.getGameScope() != null || this.getGameModelScope() != null) {
                return WegasPermission.getAsCollection(this.getEffectiveOwner().getAssociatedReadPermission());
            }
        }

        return WegasPermission.FORBIDDEN;
    }

    @Override
    public Collection<WegasPermission> getRequieredReadPermission() {
        WegasPermission perm = null;
        if (this.getTeam() != null) {
            if (this.getTeamScope().getBroadcastScope().equals("GameScope")) {
                perm = this.getTeam().getGame().getAssociatedReadPermission();
            } else {
                perm = this.getTeam().getAssociatedWritePermission();
            }
        } else if (this.getPlayer() != null) {
            if (this.getPlayerScope().getBroadcastScope().equals("TeamScope")) {
                perm = this.getPlayer().getTeam().getAssociatedWritePermission();
            } else if (this.getPlayerScope().getBroadcastScope().equals("GameScope")) {
                perm = this.getPlayer().getGame().getAssociatedReadPermission();
            } else {
                perm = this.getPlayer().getAssociatedWritePermission();
            }
        } else if (this.getGame() != null) {
            perm = this.getGame().getAssociatedReadPermission();
        } else if (this.gameModelScope != null) {
            // this.getGameModel().getChannel();
            perm = this.getGameModelScope().getVariableDescriptor().getGameModel().getAssociatedReadPermission();
        } else {
            return this.getDefaultDescriptor().getGameModel().getRequieredReadPermission();
        }
        return WegasPermission.getAsCollection(perm);
    }
}
