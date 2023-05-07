package it.xmrblake.telegrampoll.tables;

import it.arenacraft.data.core.api.mysql.MysqlConnection;
import it.arenacraft.mini.orm.api.query.executors.ILoadQuery;
import it.arenacraft.mini.orm.api.query.executors.IUpdateQuery;
import it.arenacraft.mini.orm.api.service.IMiniORMQueryService;
import it.xmrblake.telegrampoll.model.User;
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

    public void insertUser(long chatId, boolean isSuperAdmin, boolean isAccepted, MysqlConnection connection) throws Exception {
        insertPendingUser.insert(User.class).value("chatId", chatId)
                .value("superAdmin", isSuperAdmin)
                .value("accepted", isAccepted)
                .update(connection);
    }

    public void updateUser(MysqlConnection connection, User user) throws Exception {
        updateUserAccess.update(User.class)
                .set("accepted", user.isAccepted())
                .where("chatId", user.getChatId())
                .update(connection);
    }

    public Optional<User> selectPendingUser(MysqlConnection connection, long chatId) throws Exception {
        return Optional.ofNullable(selectPendingUser.selectAll(User.class)
                .where("chatId", chatId)
                .readFirstOrNull(connection, User.class));
    }

    public List<User> selectAllUsers(MysqlConnection connection) throws Exception {
        return selectAllUsers.selectAll(User.class)
                .readAll(connection, User.class);
    }

    public List<User> selectAdmins(MysqlConnection connection) throws Exception {
        return selectAdmins.selectAll(User.class)
                .where("superAdmin", true)
                .readAll(connection, User.class);
    }

    public List<User> selectLogged(MysqlConnection connection) throws Exception {
        return  selectLogged.selectAll(User.class)
                .where("accepted", true)
                .readAll(connection, User.class);
    }
}
