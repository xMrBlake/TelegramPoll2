package it.xmrblake.telegrampoll.tables;

import it.arenacraft.data.core.api.mysql.MysqlConnection;
import it.arenacraft.data.core.api.mysql.MysqlStatement;
import it.arenacraft.mini.orm.api.query.executors.ILoadQuery;
import it.arenacraft.mini.orm.api.query.executors.IUpdateQuery;
import it.arenacraft.mini.orm.api.service.IMiniORMQueryService;
import it.xmrblake.telegrampoll.model.Vote;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.With;

import java.util.List;

@Getter
@AllArgsConstructor
@With
public class VotesTable {

    private final IUpdateQuery insertVote;
    private final IUpdateQuery updateVote;
    private final IUpdateQuery dropTable;
    private final IUpdateQuery dropValue;
    private final ILoadQuery selectVotations;
    private final ILoadQuery selectAllVotes;

    public VotesTable(IMiniORMQueryService queryService) {
        this.insertVote = queryService.update();
        this.updateVote = queryService.update();
        this.dropTable = queryService.update();
        this.dropValue = queryService.update();
        this.selectVotations = queryService.load();
        this.selectAllVotes = queryService.load();
    }

    public void insertVote(MysqlConnection connection, String chatId, int applicationId, String vote) throws Exception {
        insertVote.insert(Vote.class).value("chatId", chatId)
                .value("applicationId", applicationId)
                .value("vote", vote)
                .update(connection);
    }

    public void updateVote(MysqlConnection connection, Vote oldVote, String vote) throws Exception {
        updateVote.update(Vote.class).set("vote", vote)
                .where("chatId", oldVote.getChatId())
                .where("applicationId", oldVote.getChatId())
                .update(connection);
    }

    public void dropVotes(MysqlConnection connection) throws Exception {
        //TODO nuovo da controllare
        dropValue.delete(Vote.class).update(connection);
    }

    public List<Vote> selectVotes(MysqlConnection connection, int index) throws Exception {
        return selectVotations.selectAll(Vote.class).where("applicationid", index).readAll(connection, Vote.class);
    }

    public List<Vote> selectAllVotes(MysqlConnection connection) throws Exception {
        return selectAllVotes.selectAll(Vote.class).readAll(connection, Vote.class);
    }


}
