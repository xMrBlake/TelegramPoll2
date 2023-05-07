package it.xmrblake.telegrampoll.service;

import it.xmrblake.telegrampoll.TelegramPollPlugin;
import it.xmrblake.telegrampoll.config.InlineKeyboardMarkupConfig;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static it.arenacraft.essentials3.util.localization.Localizer.local;

public class KeyboardService {

    private final TelegramPollPlugin plugin;
    private final HashMap<String, InlineKeyboardMarkupConfig> keyboards;
    private static final String LANG_PREFIX = TelegramPollPlugin.BASE_LANG_PREFIX + "voting-keyboard.";

    public KeyboardService(TelegramPollPlugin plugin){
        this.plugin = plugin;
        this.keyboards = plugin.getKeyboards();
    }

    public InlineKeyboardMarkup buildKeyboard(String keyboardName) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(keyboards.get(keyboardName).getKeyboard());
        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup buildKeyboard(List<List<InlineKeyboardButton>> inlineKeyboard) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(inlineKeyboard);
        return inlineKeyboardMarkup;
    }

    public List<List<InlineKeyboardButton>> modifyButton(String keyboardName, int list1, int list2, String placeholder, String replaced) {
        List<List<InlineKeyboardButton>> keyboard = keyboards.get(keyboardName).getKeyboard();
        keyboard.get(list1).get(list2).getText().replace(placeholder, replaced);
        return keyboard;
    }

    public List<List<InlineKeyboardButton>> modifyButton(List<List<InlineKeyboardButton>> keyboard, int list1, int list2, String placeholder, String replaced) {
        keyboard.get(list1).get(list2).getText().replace(placeholder, replaced);
        return keyboard;
    }

    public InlineKeyboardMarkup createKeyboardForRole(int index, int voteCode){
        Set<String> keySet = plugin.getRoles().keySet();
        String[] indexedKey = keySet.toArray(new String[keySet.size()]);
        List<String> entries = plugin.getRoles().get(indexedKey[index]);
        List<List<InlineKeyboardButton>> buttons = new ArrayList<>();
        List<InlineKeyboardButton> button = new ArrayList<>();
        for(String name : entries){
            if(entries.size() >= 3){
                List<InlineKeyboardButton> row = new ArrayList<>();
                row.add(new InlineKeyboardButton(local(LANG_PREFIX + "-favorevole", name),null, "application-positive-" + index + "-" + name + "-" + voteCode , null, null, null, false, null, null ));
                buttons.add(row);
                continue;
            }
            button.add(new InlineKeyboardButton(local(LANG_PREFIX + "-favorevole", name),null, "application-positive-" + index + "-" + name + "-" + voteCode, null, null, null, false, null, null ));
        }
        buttons.add(button);
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row2.add(createButton(local(LANG_PREFIX + "-contrario"), "application-reject-" + index + "-" + voteCode));
        row2.add(createButton(local(LANG_PREFIX + "-astenuto"), "application-astenuto-" + index + "-" + voteCode));
        buttons.add(row2);
        List<InlineKeyboardButton> row3 = new ArrayList<>();
        row3.add(createButton(local(LANG_PREFIX + "blank"), "application-blank-" + index + "-" + voteCode));
        buttons.add(row3);
        List<InlineKeyboardButton> row4 = new ArrayList<>();
        row4.add(createButton(local(LANG_PREFIX + "nullo"), "application-null-" + index  + "-" + voteCode));
        buttons.add(row4);
        return new InlineKeyboardMarkup(buttons);
    }
    public InlineKeyboardMarkup createResultsKeyboard(int index, int max){
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        List<InlineKeyboardButton> buttons = new ArrayList<>();
        if(index -1 > 0){
            buttons.add(createButton(local(LANG_PREFIX + "indietro"), "results-indietro-" + (index - 1)));
        }
        if(index + 1 < max){
            buttons.add(createButton(local(LANG_PREFIX + "avanti"), "results-avanti-" + (index +1)));
        }
        keyboard.add(buttons);
        return new InlineKeyboardMarkup(keyboard);
    }

    public EditMessageReplyMarkup modifyReplyKeyboard(Message message, InlineKeyboardMarkup keyboardMarkup){
        EditMessageReplyMarkup newMessage = new EditMessageReplyMarkup();
        newMessage.setChatId(message.getChatId());
        newMessage.setMessageId(message.getMessageId());
        newMessage.setReplyMarkup(keyboardMarkup);
        return newMessage;
    }

    public InlineKeyboardButton createButton(String text, String callBackData){
        return new InlineKeyboardButton(text, null, callBackData, null, null, null, null, null, null);
    }

}
