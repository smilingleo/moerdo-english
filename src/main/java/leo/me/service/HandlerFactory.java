package leo.me.service;

import static leo.me.Constants.*;

import leo.me.lambda.MoerdoRequest;

public class HandlerFactory {
    public static Handler getHandler(MoerdoRequest request) {
        if (CMD_READ_WORDS.equalsIgnoreCase(request.getCommand())) {
            return new ReadNewWordsHandler();
        } else if (CMD_READ_TEXT.equalsIgnoreCase(request.getCommand())) {
            return new ReadTextHandler();
        } else if (CMD_LIST_HISTORY.equalsIgnoreCase(request.getCommand())) {
            return new ListHistoryHandler();
        } else if (CMD_CHANGE_USER.equalsIgnoreCase(request.getCommand())) {
            return new UserManagementHandler();
        } else {
            throw new IllegalArgumentException("unknown command:" + request.getCommand());
        }
    }
}
