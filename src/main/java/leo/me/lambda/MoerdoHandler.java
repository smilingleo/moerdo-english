package leo.me.lambda;

import static leo.me.Constants.CMD_READ_WORDS;

import com.amazonaws.services.lambda.runtime.Context;
import leo.me.service.HandlerFactory;

public class MoerdoHandler {


    public MoerdoResponse handleRequest(MoerdoRequest request, Context context) {
        if (request.getCommand() == null) {
            request.setCommand(CMD_READ_WORDS);
        }

        return HandlerFactory.getHandler(request).handle(request);
    }


}
