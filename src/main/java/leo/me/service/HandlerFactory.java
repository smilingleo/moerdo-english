package leo.me.service;

import static java.lang.String.format;
import static leo.me.Constants.*;

import leo.me.anki.AnkiWebClient;
import leo.me.exception.ClientSideException;
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
        } else if (CMD_GET_OPENID.equalsIgnoreCase(request.getCommand())) {
            return new GetOpenIdHandler();
        } else if (CMD_ANKI_LIST_DECK.equalsIgnoreCase(request.getCommand())) {
            return new AnkiListDeckHandler(new AnkiWebClient());
        } else if (CMD_ANKI_GET_CARDS.equalsIgnoreCase(request.getCommand())) {
            return new AnkiGetCardsHandler(new AnkiWebClient());
        } else if (CMD_ANKI_SET_DECK.equalsIgnoreCase(request.getCommand())) {
            return new AnkiSetDeckHandler(new AnkiWebClient());
        } else if (CMD_GET_IMAGES.equals(request.getCommand())) {
            return new GetImageHandler();
        } else if (CMD_GET_RELATED_WORDS.equals(request.getCommand())) {
            return new GetRelatedWordsHandler();
        } else if (CMD_UPLOAD_IMAGE.equals(request.getCommand())) {
            return new FileUploadHandler();
        } else {
            throw new ClientSideException(format("不支持的命令:%s, 请通过《世凝听记》小程序调用API", request.getCommand()));
        }
    }
}
