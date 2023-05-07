package it.xmrblake.telegrampoll.tables;

import it.arenacraft.data.core.api.mysql.MysqlConnection;
import it.arenacraft.data.core.api.mysql.MysqlStatement;
import it.arenacraft.mini.orm.api.query.executors.ILoadQuery;
import it.arenacraft.mini.orm.api.query.executors.IUpdateQuery;
import it.arenacraft.mini.orm.api.service.IMiniORMQueryService;
import it.xmrblake.telegrampoll.model.Vote;
import it.xmrblake.telegrampoll.model.VoteObject;
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

    public void insertVote(MysqlConnection connection, VoteObject vote) throws Exception {
        //INSERT_VOTE.initializeIfEmpty(()-> UpdateQueryParser.init(QueryBuilder.insert(this))).updateFromObjectFields(connection, vote, List.of());
        insertVote.insert(Vote.class).value("chatid", vote.getChatId()).value("applicationid", vote.getApplicationId()).value("vote", vote.getVote()).update(connection);
    }

    public void updateVote(MysqlConnection connection, Vote oldVote, VoteObject newVote) throws Exception {
        /*UPDATE_VOTE.initializeIfEmpty(
                () -> UpdateQueryParser.init(QueryBuilder.updateAndSet(this).where(this, "chat_id"))
        ).updateFromObjectFields(connection, newVote, List.of(oldVote.getChatId()));*/
        updateVote.update(Vote.class).value("vote", newVote.getVote()) .where("chatid", oldVote.getChatid()).where("applicationid", oldVote.getChatid()).update(connection);
    }

    public void dropVotes(MysqlConnection connection) throws Exception {
        //DROP_TABLE.initializeIfEmpty(() -> UpdateQueryParser.init(QueryBuilder.delete(this))).update(connection, List.of());
        try(MysqlStatement statement = connection.update("DELETE * FROM tp_votes")){

        }
    }

    public List<Vote> selectVotes(MysqlConnection connection, int index) throws Exception {
       // return SELECT_VOTATIONS.initializeIfEmpty(()-> ReadQueryParser.init(deserializersMapHolder, this, QueryBuilder.selectAll(this).where(
         //       Where.of(this, "application_id")))).readAll(connection, List.of(index), new TypeToken<List<Vote>>(){}.getType());
        return selectVotations.selectAll(Vote.class).where("applicationid", index).readAll(connection, Vote.class);
    }

    public List<Vote> selectAllVotes(MysqlConnection connection) throws Exception {
        //return SELECT_ALL_VOTES.initializeIfEmpty(
          //      () -> ReadQueryParser.init(deserializersMapHolder, this, QueryBuilder.selectAll(this))).readAll(connection, List.of(), new TypeToken<List<Vote>>(){}.getType());
        return selectAllVotes.selectAll(Vote.class).readAll(connection, Vote.class);
    }


}
