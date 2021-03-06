/*
 * Wegas
 * http://wegas.albasim.ch
 *
 * Copyright (c) 2013-2018 School of Business and Engineering Vaud, Comem, MEI
 * Licensed under the MIT License
 */
package com.wegas.core.security.ejb;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ILock;
import com.wegas.core.Helper;
import com.wegas.core.ejb.BaseFacade;
import com.wegas.core.ejb.GameFacade;
import com.wegas.core.ejb.PlayerFacade;
import com.wegas.core.exception.client.WegasConflictException;
import com.wegas.core.exception.client.WegasErrorMessage;
import com.wegas.core.exception.client.WegasNotFoundException;
import com.wegas.core.exception.internal.WegasNoResultException;
import com.wegas.core.persistence.game.Game;
import com.wegas.core.persistence.game.GameModel;
import com.wegas.core.persistence.game.Player;
import com.wegas.core.persistence.game.Team;
import com.wegas.core.rest.util.Email;
import com.wegas.core.security.aai.AaiAccount;
import com.wegas.core.security.guest.GuestJpaAccount;
import com.wegas.core.security.guest.GuestToken;
import com.wegas.core.security.jparealm.JpaAccount;
import com.wegas.core.security.persistence.AbstractAccount;
import com.wegas.core.security.persistence.Permission;
import com.wegas.core.security.persistence.Role;
import com.wegas.core.security.persistence.User;
import com.wegas.core.security.util.AuthenticationInformation;
import com.wegas.messaging.ejb.EMailFacade;
import java.util.*;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Schedule;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.naming.NamingException;
import javax.persistence.Query;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.crypto.RandomNumberGenerator;
import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.apache.shiro.subject.Subject;
import org.slf4j.LoggerFactory;

/**
 * @author Francois-Xavier Aeberhard (fx at red-agent.com)
 */
@Stateless
@LocalBean
public class UserFacade extends BaseFacade<User> {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(UserFacade.class);

    @Inject
    private HazelcastInstance hzInstance;

    /**
     *
     */
    @EJB
    private AccountFacade accountFacade;

    /**
     *
     */
    @EJB
    private RoleFacade roleFacade;

    /**
     *
     */
    @EJB
    private PlayerFacade playerFacade;

    /**
     *
     */
    @EJB
    private GameFacade gameFacade;

    /**
     *
     */
    public UserFacade() {
        super(User.class);
    }

    /**
     * Login as guest
     *
     * @return the just logged user
     *
     * @throws WegasErrorMessage when guest not allowed
     */
    public User guestLogin() {
        if (Helper.getWegasProperty("guestallowed").equals("true")) {
            User newUser = new User();
            this.create(newUser);
            //this.setCurrentUser(newUser);

            newUser.addAccount(new GuestJpaAccount());
            this.merge(newUser);

            Subject subject = SecurityUtils.getSubject();
            subject.login(new GuestToken(newUser.getMainAccount().getId()));

            return newUser;
        }
        throw WegasErrorMessage.error("Guest log in not allowed on this server");
    }

    /**
     * Check is username is already in use
     *
     * @param username username to check
     *
     * @return true is username is already in use
     */
    public boolean checkExistingUsername(String username) {
        return this.getUserByUsername(username) != null;
    }

    public User authenticate(AuthenticationInformation authInfo) {
        Subject subject = SecurityUtils.getSubject();

        User guest = null;
        if (subject.isAuthenticated()) {
            AbstractAccount gAccount = accountFacade.find((Long) subject.getPrincipal());
            if (gAccount instanceof GuestJpaAccount) {
                logger.error("Logged as guest");
                guest = gAccount.getUser();
                subject.logout();
            }
        }

        //if (!currentUser.isAuthenticated()) {
        UsernamePasswordToken token = new UsernamePasswordToken(authInfo.getLogin(), authInfo.getPassword());
        token.setRememberMe(authInfo.isRemember());
        try {
            subject.login(token);
            if (authInfo.isAgreed()) {
                AbstractAccount account = accountFacade.find((Long) subject.getPrincipal());
                if (account instanceof JpaAccount) {
                    ((JpaAccount) account).setAgreedTime(new Date());
                }
            }

            User user = this.getCurrentUser();

            if (guest != null) {
                this.transferPlayers(guest, user);
            }
            return user;
        } catch (AuthenticationException aex) {
            throw WegasErrorMessage.error("Email/password combination not found");
        }
    }

    public User signup(JpaAccount account) throws AddressException, WegasConflictException {
        Helper.assertEmailPattern(account.getEmail());

        if (account.getUsername().equals("") || !this.checkExistingUsername(account.getUsername())) {
            User user;
            Subject subject = SecurityUtils.getSubject();

            if (subject.isAuthenticated() && accountFacade.find((Long) subject.getPrincipal()) instanceof GuestJpaAccount) {
                /**
                 * Subject is authenticated as guest but try to signup with a
                 * full account -> let's upgrade
                 */
                GuestJpaAccount from = (GuestJpaAccount) accountFacade.find((Long) subject.getPrincipal());
                subject.logout();
                return this.upgradeGuest(from, account);
            } else {
                // Check if e-mail is already taken and if yes return a localized error message:
                try {
                    accountFacade.findByEmail(account.getEmail());
                    throw new WegasConflictException("email");
                } catch (WegasNoResultException e) {
                    // GOTCHA
                    // E-Mail not yet registered -> proceed with account creation
                    user = new User(account);
                    this.create(user);
                    return user;
                }
            }
        } else {
            throw new WegasConflictException("username");
        }
    }

    /**
     * logout current user
     */
    public void logout() {
        Subject subject = SecurityUtils.getSubject();
        if (subject.isRunAs()) {
            subject.releaseRunAs();
        } else {
            subject.logout();
        }
    }

    /**
     * @return a User entity, based on the shiro login state
     */
    public AbstractAccount getCurrentAccount() {
        final Subject subject = SecurityUtils.getSubject();

        if (subject.isRemembered() || subject.isAuthenticated()) {
            return accountFacade.find((Long) subject.getPrincipal());
        }
        throw new WegasNotFoundException("Unable to find an account");
    }

    /**
     * @return a User entity, based on the shiro login state
     *
     * @throws WegasNotFoundException no current user
     */
    public User getCurrentUser() throws WegasNotFoundException {
        User currentUser = requestManager.getCurrentUser();
        if (currentUser != null) {
            return currentUser;
        } else {
            throw new WegasNotFoundException("Unable to find user");
        }
    }

    /**
     * Same as {@link #getCurrentUser() } but return null rather than throwing an exception
     *
     * @return the current user or null if current subject is not authenticated
     */
    public User getCurrentUserOrNull() {
        return requestManager.getCurrentUser();
    }


    /*private void setCurrentUser(User user) {
        requestManager.setCurrentUser(user);
    }*/
    /**
     * @param username String representing the username
     *
     * @return a User entity, based on the username
     */
    public User getUserByUsername(String username) {
        User u = null;
        try {
            u = accountFacade.findByUsername(username).getUser();
        } catch (WegasNoResultException e) {
        }
        return u;
    }

    /**
     * @param persistentId String representing the user
     *
     * @return a User entity, based on the persistentId
     */
    public User getUserByPersistentId(String persistentId) {
        User u = null;
        try {
            u = accountFacade.findByPersistentId(persistentId).getUser();
        } catch (WegasNoResultException e) {
        }
        return u;
    }

    @Override
    public void create(User user) {
//        AbstractAccount account = user.getMainAccount();
        /*
        // The following check is now done by caller UserController.signup()
        try {
            if (account instanceof JpaAccount) {                                // @fixme This is only done to have a nice error and not the unparsable ConstraintViolationException
                String mail = ((JpaAccount) account).getEmail();
                if (mail != null && !mail.isEmpty()) {
                    accountFacade.findByEmail(mail);
                    throw WegasErrorMessage.error("This email is already associated with an existing account.");
                }
            }
        } catch (WegasNoResultException | EJBTransactionRolledbackException e) {
            // GOTCHA
            // E-Mail not yet registered -> proceed
        }
         */

        getEntityManager().persist(user);

        /*
         * Very strange behaviour: without this flush, RequestManages faild to be injected within others beans...
         */
        this.getEntityManager().flush();
    }

    @Override
    public void remove(User entity) {
        for (Role r : entity.getRoles()) {
            r.removeUser(entity);
        }
        /* ???: Should be cascaded, nope ??? */
        // clone list to avoid CME
        List<AbstractAccount> accounts = new ArrayList<>(entity.getAccounts());
        for (AbstractAccount aa : accounts) {
            accountFacade.remove(aa);
        }

        for (Player player : entity.getPlayers()) {
            player.setUser(null);
        }

        for (Team team : entity.getTeams()) {
            team.setCreatedBy(null);
        }

        getEntityManager().remove(entity);
    }

    /**
     * Get all roles which have some permissions on the given instance..
     * <p>
     * Map is { id : role id, name: role name, permissions: list of permissions
     * related to instance}
     *
     * @param instance
     *
     * @return list of "Role"
     */
    public List<Map> findRolePermissionByInstance(String instance) {
        TypedQuery<Role> findByToken = getEntityManager().createQuery("SELECT DISTINCT roles FROM Role roles JOIN roles.permissions p WHERE p.value LIKE :instance", Role.class);//@fixme Unable to select role with a like w/ embeddebale
        // Query findByToken = getEntityManager().createQuery("SELECT DISTINCT roles FROM Role roles WHERE roles.permissions.value = 'mm'");
        // SELECT DISTINCT roles FROM Role roles WHERE roles.permissions LIKE :gameId
        findByToken.setParameter("instance", "%:" + instance);

        List<Role> res = findByToken.getResultList();
        List<Map> allRoles = new ArrayList<>();
        for (Role unRole : res) {
            Map<String, Object> role = new HashMap<>();
            role.put("id", unRole.getId());
            role.put("name", unRole.getName());
            List<String> permissions = new ArrayList<>();
            role.put("permissions", permissions);

            for (Permission permission : unRole.getPermissions()) {
                String splitedPermission[] = permission.getValue().split(":");
                if (splitedPermission.length >= 3) {
                    if (splitedPermission[2].equals(instance)) {
                        permissions.add(permission.getValue());
                    }
                }
            }
            allRoles.add(role);
        }

        return allRoles;
    }

    /**
     * Create role_permissions
     *
     * @param roleId     id of the role to add permission too
     * @param permission permission to add
     *
     * @return true if the permission has successfully been added
     */
    public boolean addRolePermission(final Long roleId, final String permission) {
        final Role r = roleFacade.find(roleId);
        return r.addPermission(new Permission(permission));
    }

    /**
     *
     * @param userId     id of the user
     * @param permission permission to add
     *
     * @return true if the permission has successfully been added
     */
    public boolean addUserPermission(final Long userId, final String permission) {
        return this.addUserPermission(userId, new Permission(permission));
    }

    /**
     * @param userId id of the user
     * @param p      permission to add
     *
     * @return true if the permission has successfully been added
     */
    public boolean addUserPermission(final Long userId, final Permission p) {
        final User user = this.find(userId);
        return user.addPermission(p);
    }

    /**
     *
     * @param user
     * @param permission
     *
     * @return true if the permission has successfully been added
     */
    public boolean addUserPermission(final User user, final String permission) {
        return user.addPermission(new Permission(permission));
        //return user.addPermission(this.generatePermisssion(permission));
    }

    /**
     * @param instance
     *
     * @return all user which have a permission related to the given instance
     */
    public List<User> findUserByPermissionInstance(String instance) {
        final TypedQuery<User> findByToken = getEntityManager().createNamedQuery("User.findUserPermissions", User.class);
        findByToken.setParameter("instance", "%:" + instance);
        return findByToken.getResultList();
    }

    /**
     * @param instance
     *
     * @return all user which have a permission related to the given instance
     */
    public List<User> findEditors(String instance) {
        String permission;
        if (instance.substring(0, 2).equals("gm")) {
            permission = "GameModel:%Edit%:";
        } else {
            permission = "Game:%Edit%:";
        }

        final TypedQuery<User> findByToken = getEntityManager().createNamedQuery("User.findUserPermissions", User.class);
        findByToken.setParameter("instance", permission + instance);
        return findByToken.getResultList();
    }

    /**
     * Get all users is
     *
     * @param role_id
     *
     * @return all role members
     */
    public List<User> findUsersWithRole(Long role_id) {
        /* Why not using JPA ?
        return roleFacade.find(role_id).getUsers(); ??????
         */
        final TypedQuery<User> findWithRole = getEntityManager().createNamedQuery("User.findUsersWithRole", User.class);
        findWithRole.setParameter("role_id", role_id);
        return findWithRole.getResultList();
    }

    private void deletePermission(Permission p) {
        if (p.getUser() != null) {
            this.find(p.getUser().getId()).removePermission(p);
        }
        if (p.getRole() != null) {
            roleFacade.find(p.getRole().getId()).removePermission(p);
        }
    }

    public List<Role> findRoles(User user) {
        if (user != null) {
            TypedQuery<Role> queryRoles = getEntityManager().createNamedQuery("Roles.findByUser", Role.class);
            queryRoles.setParameter("userId", user.getId());
            return queryRoles.getResultList();
        } else {
            return new ArrayList<>();
        }
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public List<Role> findRolesTransactional(Long userId) {
        return this.findRoles(this.find(userId));
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public List<String> findRoles_native(User user) {
        Query queryRoles = getEntityManager().createNamedQuery("Roles.findByUser_native", Role.class);
        queryRoles.setParameter(1, user.getId());
        return queryRoles.getResultList();
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public List<Permission> findAllUserPermissionsTransactional(Long userId) {
        return this.findAllUserPermissions(this.find(userId));
    }

    public List<Permission> findAllUserPermissions(User user) {
        if (user != null) {
            List<Permission> perms = new ArrayList<>();

            for (Role role : this.findRoles(user)) {
                perms.addAll(role.getPermissions());
            }
            perms.addAll(user.getPermissions());

            return perms;
        } else {
            return new ArrayList<>();
        }
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public List<String> findAllUserPermissions_NATIVEJPA(User user) {
        Query query = getEntityManager().createNamedQuery("Permission.findByUser_native");
        query.setParameter(1, user.getId());
        return query.getResultList();
    }

    public List<Permission> findAllUserPermissions_JPA(User user) {
        List<Permission> perms = new ArrayList<>();

        for (Role role : this.findRoles(user)) {
            TypedQuery<Permission> queryRolePermission = getEntityManager().createNamedQuery("Permission.findByRole", Permission.class);
            queryRolePermission.setParameter("roleId", role.getId());
            perms.addAll(queryRolePermission.getResultList());
        }

        TypedQuery<Permission> queryUserPermissions = getEntityManager().createNamedQuery("Permission.findByUser", Permission.class);
        queryUserPermissions.setParameter("userId", user.getId());
        perms.addAll(queryUserPermissions.getResultList());

        return perms;
    }

    private List<Permission> findUserPermissions(String permission, User user) {
        TypedQuery<Permission> query = getEntityManager().createNamedQuery("Permission.findByPermissionAndUser", Permission.class);
        query.setParameter("userId", user.getId());
        query.setParameter("permission", permission);

        return query.getResultList();
    }

    public void deletePermissions(User user, String permission) {
        for (Permission p : this.findUserPermissions(permission, user)) {
            this.deletePermission(p);
        }
    }

    public void deletePermissions(Game game) {
        this.deletePermissions("Game:%:g" + game.getId());
    }

    public void deletePermissions(GameModel gameModel) {
        this.deletePermissions("GameModel:%:gm" + gameModel.getId());
    }

    private void deletePermissions(String permission) {
        TypedQuery<Permission> query = getEntityManager().createNamedQuery("Permission.findByPermission", Permission.class);
        query.setParameter("permission", permission);

        for (Permission p : query.getResultList()) {
            this.deletePermission(p);
        }
    }

    /**
     *
     * @param trainerId
     * @param gameId
     */
    public void addTrainerToGame(Long trainerId, Long gameId) {
        Game game = gameFacade.find(gameId);
        User user = this.find(trainerId);
        this.addUserPermission(user, "Game:View,Edit:g" + gameId);
    }

    public void removeTrainer(Long gameId, User trainer) {

        if (this.getCurrentUser().equals(trainer)) {
            throw WegasErrorMessage.error("Cannot remove yourself");
        }

        if (this.findEditors("g" + gameId).size() <= 1) {
            throw WegasErrorMessage.error("Cannot remove last trainer");
        } else {
            this.deletePermissions(trainer, "Game:%Edit%:g" + gameId);
        }
    }

    /**
     *
     * @param userId
     * @param gameModelId
     * @param permissions
     */
    public void grantGameModelPermissionToUser(Long userId, Long gameModelId, String permissions) {
        User user = this.find(userId);

        /*
         * Revoke previous permissions (do not use removeScenarist method since
         * this method prevents to remove one own permissions,
         */
        this.deletePermissions(user, "GameModel:%:gm" + gameModelId);

        // Grant new permission
        this.addUserPermission(user, "GameModel:" + permissions + ":gm" + gameModelId);
    }

    public void removeScenarist(Long gameModelId, User scenarist) {
        if (this.getCurrentUser().equals(scenarist)) {
            throw WegasErrorMessage.error("Cannot remove yourself");
        }

        if (this.findEditors("gm" + gameModelId).size() <= 1) {
            throw WegasErrorMessage.error("Cannot remove last scenarist");
        } else {
            //remove all permission matching  both gameModelId and userId
            this.deletePermissions(scenarist, "GameModel:%:gm" + gameModelId);
        }
    }

    /**
     * @param email
     */
    public void sendNewPassword(String email) {
        try {
            requestManager.su();
            JpaAccount acc = accountFacade.findJpaByEmail(email);
            EMailFacade emailFacade = new EMailFacade();
            RandomNumberGenerator rng = new SecureRandomNumberGenerator();
            String newPassword = rng.nextBytes().toHex().substring(0, 12);
            String subject = "Wegas account";
            String body = "A new password for your wegas account has been successfully created: " + newPassword;
            String from = "noreply@" + Helper.getWegasProperty("mail.default_domain");
            if (acc != null) {
                acc.setPassword(newPassword);
                acc.setPasswordHex(null);                                           //force JPA update
                emailFacade.send(acc.getEmail(), from, null, subject, body, Message.RecipientType.TO, "text/plain; charset=utf-8", true);
            }
            this.flush();
        } catch (WegasNoResultException | MessagingException ex) {
            logger.error("Error while sending new password for email: {}", email);
        } finally{
            requestManager.releaseSu();
        }
    }

    /*
    ** Sends the given email as one separate message per addressee (as a measure against spam filters)
    ** and an additional one to the sender to provide him a copy of the message.
    ** If an address is invalid (but syntactically correct), it should not prevent from sending to the other addressees.
     */
    public void sendEmail(Email email) /* throws MessagingException */ {
        int nbExceptions = 0;
        EMailFacade emailFacade = new EMailFacade();
        for (Player p : email.getTo()) {
            Player rP = playerFacade.find(p.getId());
            AbstractAccount mainAccount = rP.getUser().getMainAccount();
            if (mainAccount instanceof JpaAccount || mainAccount instanceof AaiAccount) {
                try {
                    emailFacade.send(mainAccount.getEmail(), email.getFrom(), email.getReplyTo(), email.getSubject(), email.getBody(), Message.RecipientType.TO, "text/html; charset=utf-8", true);
                } catch (MessagingException e) {
                    nbExceptions++;
                }
            }
        }
        try {
            // Send a last message directly to the sender as a confirmation copy
            emailFacade.send(email.getReplyTo(), email.getFrom(), email.getReplyTo(), email.getSubject(), email.getBody(), Message.RecipientType.TO, "text/html; charset=utf-8", true);
        } catch (MessagingException e) {
            nbExceptions++;
        }
        if (nbExceptions > 0) {
            throw WegasErrorMessage.error(nbExceptions + " error(s) while sending email");
        }
    }

    /*
     * @FIXME Should also remove players, created games and game models
     */
    /**
     * Remove old idle guests
     */
    @Schedule(hour = "4", minute = "12")
    public void removeIdleGuests() {
        requestManager.su();
        try {
            ILock lock = hzInstance.getLock("UserFacade.Schedule");

            if (lock.tryLock()) {
                try {
                    logger.info("removeIdleGuests(): unused guest accounts will be removed");
                    TypedQuery<GuestJpaAccount> findIdleGuests = getEntityManager().createQuery("SELECT DISTINCT account FROM GuestJpaAccount account "
                            + "WHERE account.createdTime < :idletime", GuestJpaAccount.class);
                    Calendar calendar = Calendar.getInstance();
                    calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) - 3);
                    findIdleGuests.setParameter("idletime", calendar.getTime(), TemporalType.DATE);

                    List<GuestJpaAccount> resultList = findIdleGuests.getResultList();

                    for (GuestJpaAccount account : resultList) {
                        this.remove(account.getUser());
                    }

                    //Force flush before closing RequestManager !
                    getEntityManager().flush();

                    logger.info("removeIdleGuests(): {} unused guest accounts removed (idle since: {})", resultList.size(), calendar.getTime());

                } finally {
                    lock.unlock();
                    lock.destroy();
                }
            }
        } finally {
            requestManager.releaseSu();
        }
    }

    /**
     * Is the given playerId identify a player owned by the current user players
     * ?
     *
     * @param playerId
     *
     * @return true if the player is owned by the current user
     */
    public boolean matchCurrentUser(Long playerId) {
        return this.getCurrentUser().equals(playerFacade.find(playerId).getUser());
    }

    /**
     *
     * @param roleNames
     * @param user
     *
     * @return true if user is member of at least one group from the list
     */
    public boolean hasAnyRole(User user, List<String> roleNames) {
        Collection<Role> roles = user.getRoles();
        if (roleNames != null && roles != null) {
            for (Role role : roles) {
                if (roleNames.contains(role.getName())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Transfer players and permission from one user to another
     *
     * @param from the player to take perm and players from
     * @param to   the whow
     */
    public void transferPlayers(User from, User to) {
        final List<Long> gameIds = new ArrayList<>();
        for (Player player : to.getPlayers()) {
            gameIds.add(player.getGame().getId());
        }
        for (Player p : from.getPlayers()) {
            if (!gameIds.contains(p.getGame().getId())) { // User already has a player in p's game
                p.setName(to.getName());
                p.setUser(to);
            }
        }
        for (Permission p : from.getPermissions()) {
            p.setUser(to);
        }
    }

    public User upgradeGuest(GuestJpaAccount guest, JpaAccount account) {
        User user = guest.getUser();
        user.addAccount(account);

        accountFacade.create(account);
        // Detach and delete account
        accountFacade.remove(guest.getId());

        this.refresh(user);
        for (Player p : user.getPlayers()) {
            p.setName(user.getName());
        }
        return user;
    }

    public void addRole(User u, Role r) {
        u.addRole(r);
        r.addUser(u);
        //this.merge(u);
    }

    public void addRole(Long uId, Long rId) {
        User u = this.find(uId);
        Role r = roleFacade.find(rId);
        this.addRole(u, r);
    }

    /**
     * @return Looked-up EJB
     */
    public static UserFacade lookup() {
        try {
            return Helper.lookupBy(UserFacade.class);
        } catch (NamingException ex) {
            logger.error("Error retrieving user facade", ex);
            return null;
        }
    }
}
