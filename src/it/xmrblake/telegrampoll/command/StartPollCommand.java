package it.xmrblake.telegrampoll.command;

import it.arenacraft.data.core.api.mysql.MysqlConnection;
import it.xmrblake.telegrampoll.TelegramPollPlugin;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static it.arenacraft.essentials3.util.localization.Localizer.local;

public class StartPollCommand extends BotCommand implements IBotCommand {

    private static final String COMMAND_LABEL = "voto";
    private static final String LANG_PREFIX = TelegramPollPlugin.BASE_LANG_PREFIX + "command.voto-";

    private final TelegramPollPlugin plugin;

    public StartPollCommand(TelegramPollPlugin plugin) {
        super(COMMAND_LABEL, local(LANG_PREFIX + "description"));
        this.plugin = plugin;
    }

    @Override
    public String getCommandIdentifier() {
        return COMMAND_LABEL;
    }
    @Override
    public void processMessage(AbsSender absSender, Message message, String[] arguments){
        SendMessage sendMessage;
        String chatId = message.getChatId().toString();
        if(arguments.length != 1){
            sendMessage = plugin.getMessageService().createMessage(local(LANG_PREFIX + "usage"), null, chatId);
            try {
                absSender.execute(sendMessage);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
            return;
        }
        if(!plugin.isCurrentVoting()){
            sendMessage = plugin.getMessageService().createMessage(local("not-vote-session"), null, message.getChatId().toString());
            try {
                absSender.execute(sendMessage);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
            return;
        }
        if(arguments[0].equalsIgnoreCase("12345")){
            if(plugin.isUsed()){
                sendMessage = plugin.getMessageService().createMessage(local(LANG_PREFIX + "used"), null, message.getChatId().toString());
                try {
                    absSender.execute(sendMessage);
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            }
            try {
                plugin.getVotingService().startVoting(absSender, message, Integer.parseInt(arguments[0]));
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
            return;
        }
        try(MysqlConnection connection = plugin.getMysqlConnection()){
           if(plugin.getPasswordTable().isAlreadyUsed(connection, Integer.parseInt(arguments[0]))){
               sendMessage = plugin.getMessageService().createMessage(local(LANG_PREFIX + "already-used"), null, chatId);
               absSender.execute(sendMessage);
               return;
           }
           plugin.getVotingService().startVoting(absSender, message, Integer.parseInt(arguments[0]));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
