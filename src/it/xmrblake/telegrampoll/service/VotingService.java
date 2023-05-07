package it.xmrblake.telegrampoll.service;

import it.xmrblake.telegrampoll.TelegramPollPlugin;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Set;

import static it.arenacraft.essentials3.util.localization.Localizer.local;

public class VotingService {

    private static final String LANG_PREFIX = TelegramPollPlugin.BASE_LANG_PREFIX + "voting.";
    private final TelegramPollPlugin plugin;
    private final MessageService messageService;

    public VotingService(TelegramPollPlugin plugin){
        this.plugin = plugin;
        this.messageService = plugin.getMessageService();
    }

    public void startVoting(AbsSender sender, Message message, int voteCode) throws TelegramApiException {
        SendMessage sendMessage;
        if (!plugin.isCurrentVoting()) {
            sendMessage = plugin.getMessageService()
                    .createMessage(local("not-vote-session"), null, message.getChatId().toString());
            sender.execute(sendMessage);
            return;
        }
        sendMessage = messageService.createMessage(messageService.createApplicationMessage(0),
                plugin.getKeyboardService().createKeyboardForRole(0, voteCode),
                message.getChatId().toString());
        sender.execute(sendMessage);
    }

    public void result(AbsSender sender, int applicationId, String chatId) throws TelegramApiException {
        if(plugin.isCurrentVoting()){
            SendMessage sendMessage = new SendMessage(chatId, local(LANG_PREFIX + "current-voting"));
            sender.execute(sendMessage);
            return;
        }
        Set<String> keySet = plugin.getRoles().keySet();
        String[] indexedKey = keySet.toArray(new String[keySet.size()]);

        String message = messageService.createResultMessage(applicationId, indexedKey);
        SendMessage sendMessage = messageService.createMessage(message, plugin.getKeyboardService().createResultsKeyboard(0, keySet.size()), chatId);
        sender.execute(sendMessage);
    }


}
