package it.xmrblake.telegrampoll.command;

import at.favre.lib.crypto.bcrypt.BCrypt;
import it.arenacraft.data.core.api.mysql.MysqlConnection;
import it.arenacraft.data.core.api.mysql.MysqlTransaction;
import it.xmrblake.telegrampoll.TelegramPollPlugin;
import it.xmrblake.telegrampoll.model.User;
import it.xmrblake.telegrampoll.model.Vote;
import it.xmrblake.telegrampoll.model.VoteObject;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
import java.util.Optional;

import static it.arenacraft.essentials3.util.localization.Localizer.local;

public class CancelCommand extends BotCommand implements IBotCommand {

    private static final String COMMAND_LABEL = "cancel";
    private static final String LANG_PREFIX = TelegramPollPlugin.BASE_LANG_PREFIX + "command.cancel-";
    private final TelegramPollPlugin plugin;

    public CancelCommand(TelegramPollPlugin plugin){
        super(COMMAND_LABEL, local(LANG_PREFIX + "description"));
        this.plugin = plugin;
    }


    @Override
    public String getCommandIdentifier() {
        return COMMAND_LABEL;
    }

    @Override
    public void processMessage(AbsSender absSender, Message message, String[] arguments) {
        String chatId = message.getChatId().toString();
        SendMessage sendMessage;
        if(arguments.length != 1){
            sendMessage = plugin.getMessageService().createMessage(local(LANG_PREFIX + "usage"), null, chatId);
            try {
                absSender.execute(sendMessage);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
            return;
        }
        Optional<User> sender;
        try(MysqlConnection connection = plugin.getMysqlConnection()){
            sender = plugin.getUsersTable().selectPendingUser(connection, Long.parseLong(chatId));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        if(sender.isEmpty() || sender.get().getSuperadmin()){
            sendMessage = plugin.getMessageService().createMessage(local(LANG_PREFIX + "no-permission"), null, chatId);
            try {
                absSender.execute(sendMessage);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
            return;
        }
        try(MysqlConnection connection = plugin.getMysqlConnection();
            MysqlTransaction transaction = new MysqlTransaction(connection)){
            List<Vote> votes = plugin.getVotesTable().selectAllVotes(connection);
            for(Vote vote : votes){
                BCrypt.Result verify = BCrypt.verifyer().verify(arguments[0].toCharArray(), vote.getChatid());
                if(verify.verified){
                    VoteObject cancelledVote = new VoteObject(vote.getChatid(), vote.getApplicationid(), "cancellata");
                    plugin.getVotesTable().updateVote(connection, vote, cancelledVote);
                    System.out.println("cancellata");
                }
            }
            transaction.commit();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
