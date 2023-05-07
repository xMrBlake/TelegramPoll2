package it.xmrblake.telegrampoll.command;

import it.arenacraft.data.core.api.mysql.MysqlConnection;
import it.xmrblake.telegrampoll.TelegramPollPlugin;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static it.arenacraft.essentials3.util.localization.Localizer.local;

public class LoggedCommand extends BotCommand implements IBotCommand {

    private static final String LANG_PREFIX = TelegramPollPlugin.BASE_LANG_PREFIX + "command.logged-";

    private static final String COMMAND_LABEL = "logged";

    private final TelegramPollPlugin plugin;

    public LoggedCommand(TelegramPollPlugin plugin){
        super(COMMAND_LABEL, local(LANG_PREFIX + "description"));
        this.plugin = plugin;
    }

    @Override
    public String getCommandIdentifier() {
        return COMMAND_LABEL;
    }

    @Override
    public void processMessage(AbsSender absSender, Message message, String[] arguments) {
        int logged = 0;
        try(MysqlConnection connection = plugin.getMysqlConnection()){
            logged =  plugin.getUsersTable().selectLogged(connection).size();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        try {
            absSender.execute(plugin.getMessageService().createMessage(local(LANG_PREFIX + "users-logged", String.valueOf(logged)), null, message.getChatId().toString()));
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

}
