package it.xmrblake.telegrampoll.model;

import lombok.Data;

@Data
public class UserObject {

    private final long chatId;
    private final boolean superAdmin;
    private final boolean accepted;
}
