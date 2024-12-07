package client.dto;

public enum RequestCommand {
    // Room Command
    GET_ROOM_LIST, FIND_ROOM, CREATE_ROOM, ENTER_ROOM, EXIT_ROOM, VOTE_DISCUSSION,

    // Chat Command
    GET_CHAT_HISTORY, SEND_CHAT

    ;
}
