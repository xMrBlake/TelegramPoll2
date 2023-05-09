package it.xmrblake.telegrampoll.tables;

import it.arenacraft.data.core.api.mysql.MysqlConnection;
import it.arenacraft.data.core.api.mysql.MysqlStatement;
import it.arenacraft.mini.orm.api.query.executors.ILoadQuery;
import it.arenacraft.mini.orm.api.query.executors.IUpdateQuery;
import it.arenacraft.mini.orm.api.service.IMiniORMQueryService;
import it.xmrblake.telegrampoll.model.Password;
import it.xmrblake.telegrampoll.model.PasswordObject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.With;

@Getter
@With
@AllArgsConstructor
public class PasswordTable {

    private final IUpdateQuery updatePasswordState;
    private final IUpdateQuery insertPassword;
    private final IUpdateQuery dropValue;
    private final ILoadQuery selectPassword;

    public PasswordTable(IMiniORMQueryService queryService) {
        this.updatePasswordState = queryService.update();
        this.insertPassword = queryService.update();
        this.dropValue = queryService.update();
        this.selectPassword = queryService.load();
    }

    public void updatePassword(MysqlConnection connection, PasswordObject password) throws Exception {
        updatePasswordState.update(Password.class).set("used", password.isUsed())
                .where("psw", password.getPsw())
                .update(connection);
    }

    public void insertPassword(MysqlConnection connection, PasswordObject password) throws Exception {
        insertPassword.insert(Password.class).value("psw", password.getPsw())
                .value("used", password.isUsed())
                .update(connection);
    }

    public boolean isAlreadyUsed(MysqlConnection connection, int password) throws Exception {
        Password passwordOptional = selectPassword.selectAll(Password.class)
                .where("psw", password)
                .readFirstOrNull(connection, Password.class);
        if (passwordOptional == null) {
            return false;
        }
        return passwordOptional.isUsed();
    }

    public void dropPasswords(MysqlConnection connection) throws Exception {
        //TODO nuovo da controllare
        dropValue.delete(Password.class).update(connection);
    }

}
