package it.xmrblake.telegrampoll.model;

import lombok.Data;

@Data
public class VoteObject {

    private final String chatId;
    private final int applicationId;
    private final String vote;
}
