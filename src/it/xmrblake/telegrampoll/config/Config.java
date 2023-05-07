package it.xmrblake.telegrampoll.config;

import lombok.Data;

@Data
public class Config {

    private final String username;
    private final String token;
    private final String parseMode;

}
