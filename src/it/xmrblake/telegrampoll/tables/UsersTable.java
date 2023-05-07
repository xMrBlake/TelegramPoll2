package it.xmrblake.telegrampoll.tables;

import it.arenacraft.data.core.api.mysql.MysqlConnection;
import it.arenacraft.mini.orm.api.query.executors.ILoadQuery;
import it.arenacraft.mini.orm.api.query.executors.IUpdateQuery;
import it.arenacraft.mini.orm.api.service.IMiniORMQueryService;
import it.xmrblake.telegrampoll.model.User;
import it.xmrblake.telegrampoll.model.UserObject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.With;

import java.util.List;
import java.util.Optional;

@Getter
@With
@AllArgsConstructor
public class UsersTable{

    private final IUpdateQuery insertPendingUser;
    private final IUpdateQuery updateUserAccess;
    private final ILoadQuery selectPendingUser;
    private final ILoadQuery selectAllUsers;
    private final ILoadQuery selectAdmins;
    private final ILoadQuery selectLogged;

    public UsersTable(IMiniORMQueryService queryService) {
        this.insertPendingUser = queryService.update();
        this.updateUserAccess = queryService.update();
        this.selectPendingUser = queryService.load();
        this.selectAllUsers = queryService.load();
        this.selectAdmins = queryService.load();
        this.selectLogged = queryService.load();
    }

    public void insertUser(UserObject user, MysqlConnection connection) throws Exception {
        /*INSERT_PENDING_USER.initializeIfEmpty(() -> UpdateQueryParser.init(QueryBuilder.insert(this)))
                .updateFromObjectFields(connection, user, List.of());*/
        insertPendingUser.insert(User.class).value("chatid", user.getChatId()).value("superadmin", user.isSuperAdmin()).value("accepted", user.isAccepted()).update(connection);
    }

    public void updateUser(MysqlConnection connection, User user) throws Exception {
        /*UPDATE_USER_ACCESS.initializeIfEmpty(
                        () -> UpdateQueryParser.init(QueryBuilder.updateAndSet(this).where(Where.of(this, "chat_id"))))
                .updateFromObjectFields(connection, user, List.of(user.getChatId()));*/
        updateUserAccess.update(User.class).value("accepted", user.isAccepted()).where("chatid", user.getChatid()).update(connection);
    }

    public Optional<User> selectPendingUser(MysqlConnection connection, long chatId) throws Exception {
        /*return SELECT_PENDING_USER.initializeIfEmpty(
                () -> ReadQueryParser.init(deserializersMapHolder, this, QueryBuilder.selectAll(this)
                        .where(Where.of(this, "chat_id")))).readFirst(connection, List.of(chatId), User.class);*/
        return Optional.ofNullable(selectPendingUser.selectAll(User.class).where("chatid", chatId).readFirstOrNull(connection, User.class));
    }

    public List<User> selectAllUsers(MysqlConnection connection) throws Exception {
       /* return SELECT_ALL_USERS.initializeIfEmpty(
                        () -> ReadQueryParser.init(deserializersMapHolder, this, QueryBuilder.selectAll(this)))
                .readAll(connection, List.of(), new TypeToken<List<User>>() {
                }.getType());*/
        return selectAllUsers.selectAll(User.class).readAll(connection, User.class);
    }

    public List<User> selectAdmins(MysqlConnection connection) throws Exception {
        /*return SELECT_ADMINS.initializeIfEmpty(
                        () -> ReadQueryParser.init(deserializersMapHolder, this, QueryBuilder.selectAll(this)
                                .where(Where.of(this, "super_admin"))))
                .readAll(connection, List.of(true), new TypeToken<List<User>>() {
                }.getType());*/
        return selectAdmins.selectAll(User.class).where("superadmin", true).readAll(connection, User.class);
    }

    public List<User> selectLogged(MysqlConnection connection) throws Exception {
        /*return SELECT_LOGGED.initializeIfEmpty(
                () -> ReadQueryParser.init(deserializersMapHolder, this, QueryBuilder.selectAll(this)
                        .where(this, "accepted"))).readAll(connection, List.of(true), new TypeToken<List<User>>() {
        }.getType());*/
        return  selectLogged.selectAll(User.class).where("accepted", true).readAll(connection, User.class);
    }
}
