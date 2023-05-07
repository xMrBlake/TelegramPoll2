package it.xmrblake.telegrampoll.config;

import lombok.Data;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;

@Data
public class InlineKeyboardMarkupConfig {

    private final List<List<InlineKeyboardButton>> keyboard;


}
