package it.xmrblake.telegrampoll.command;

import it.xmrblake.telegrampoll.TelegramPollPlugin;
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static it.arenacraft.essentials3.util.localization.Localizer.local;

public class MeCommand extends BotCommand implements IBotCommand {

    private static final String COMMAND_LABEL = "me";
    private static final String LANG_PREFIX = TelegramPollPlugin.BASE_LANG_PREFIX + "command.me-";

    public MeCommand(){
        super(COMMAND_LABEL, local(LANG_PREFIX + "description"));
    }

    @Override
    public String getCommandIdentifier() {
        return COMMAND_LABEL;
    }

    @Override
    public void processMessage(AbsSender absSender, Message message, String[] arguments) {
        SendMessage sendMessage = new SendMessage(message.getChatId().toString(), local(LANG_PREFIX + "result", message.getChatId().toString()));
        try {
            absSender.execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

}
