package leo.me.exception;

public class ServerSideException extends RuntimeException{

    public ServerSideException(String message) {
        super("ServerSideException:" + message);
    }

    public ServerSideException(String message, Throwable cause) {
        super("ServerSideException:" + message, cause);
    }
}
