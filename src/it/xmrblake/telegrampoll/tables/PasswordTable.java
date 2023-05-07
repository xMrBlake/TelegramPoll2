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
        /*UPDATE_PASSWORD_STATE.initializeIfEmpty(() -> UpdateQueryParser.init(QueryBuilder.updateAndSet(this)
                .where(Where.of(this, "psw"))))
                .updateFromObjectFields(connection, password, List.of(password.getPsw()));*/
        updatePasswordState.update(Password.class).value("used", password.getUsed()).where("psw", password.getPsw()).update(connection);
    }

    public void insertPassword(MysqlConnection connection, PasswordObject password) throws Exception{
        //INSERT_PASSWORD.initializeIfEmpty(()-> UpdateQueryParser.init(QueryBuilder.insert(this))).updateFromObjectFields(connection, password, List.of());
        insertPassword.insert(Password.class).value("psw", password.getPsw()).value("used", password.getUsed()).update(connection);
    }

    public boolean isAlreadyUsed(MysqlConnection connection, int password) throws Exception{
        /*Optional<Password> passwordOptional = SELECT_PASSWORD.initializeIfEmpty(() -> ReadQueryParser.init(deserializersMapHolder, this, QueryBuilder.selectAll(this)
                .where(Where.of(this, "psw")))).readFirst(connection, List.of(password), Password.class);*/
        Password passwordOptional = selectPassword.selectAll(Password.class).where("psw", password).readFirstOrNull(connection, Password.class);
        if(passwordOptional == null){
            return false;
        }
        return passwordOptional.isUsed();
    }

    public void dropPasswords(MysqlConnection connection) throws Exception{
        //DROP_VALUE.initializeIfEmpty(() -> UpdateQueryParser.init(QueryBuilder.delete(this))).update(connection, List.of());
        try(MysqlStatement statement = connection.update("DELETE * FROM tp_passwords")){

        }
    }
    
}
