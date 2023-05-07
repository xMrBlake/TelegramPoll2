package it.xmrblake.telegrampoll.command;

import it.arenacraft.data.core.api.mysql.MysqlConnection;
import it.xmrblake.telegrampoll.TelegramPollPlugin;
import it.xmrblake.telegrampoll.model.User;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Optional;

import static it.arenacraft.essentials3.util.localization.Localizer.local;

public class StartVotingCommand extends BotCommand implements IBotCommand {

    private static final String COMMAND_LABEL = "voting";
    private static final String LANG_PREFIX = TelegramPollPlugin.BASE_LANG_PREFIX + "command.voting-";
    private final TelegramPollPlugin plugin;

    public StartVotingCommand(TelegramPollPlugin plugin){
        super(COMMAND_LABEL, local(LANG_PREFIX + "description"));
        this.plugin = plugin;
    }

    @Override
    public String getCommandIdentifier() {
        return COMMAND_LABEL;
    }

    @Override
    public void processMessage(AbsSender absSender, Message message, String[] arguments) {
        Optional<User> sender;
        SendMessage sendMessage = null;
        if(arguments.length < 1 || (!arguments[0].equalsIgnoreCase("start") && !arguments[0].equalsIgnoreCase("stop"))){
            sendMessage = plugin.getMessageService().createMessage(local(LANG_PREFIX + "usage"), null, message.getChatId().toString());
            try {
                absSender.execute(sendMessage);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
            return;
        }
        try(MysqlConnection connection = plugin.getMysqlConnection()){
            sender = plugin.getUsersTable().selectPendingUser(connection, message.getChatId());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        if(sender.isEmpty() || !sender.get().isSuperAdmin()){
            sendMessage = plugin.getMessageService().createMessage(local(LANG_PREFIX + "no-permission"), null, message.getChatId().toString());
            try {
                absSender.execute(sendMessage);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
            return;
        }
        if(arguments[0].equalsIgnoreCase("start")){
            if(plugin.isCurrentVoting()){
                sendMessage = plugin.getMessageService().createMessage(local(LANG_PREFIX + "already-started"), null, message.getChatId().toString());
                try {
                    absSender.execute(sendMessage);
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
                return;
            }
            sendMessage = plugin.getMessageService().createMessage(local(LANG_PREFIX + "start-voting"), null, message.getChatId().toString());
            plugin.setCurrentVoting(true);
            try(MysqlConnection connection = plugin.getMysqlConnection()){
                plugin.getVotesTable().dropVotes(connection);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        if(arguments[0].equalsIgnoreCase("stop")){
            if(!plugin.isCurrentVoting()){
                sendMessage = plugin.getMessageService().createMessage(local(LANG_PREFIX + "already-stopped"), null, message.getChatId().toString());
                try {
                    absSender.execute(sendMessage);
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
                return;
            }
            sendMessage = plugin.getMessageService().createMessage(local(LANG_PREFIX + "stop-voting"), null, message.getChatId().toString());
            plugin.setCurrentVoting(false);
        }
        try{
            absSender.execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

}
