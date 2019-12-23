package leo.me.exception;

public class ClientSideException extends RuntimeException {

    public ClientSideException(String message) {
        super("ClientSideException:" + message);
    }

    public ClientSideException(String message, Throwable cause) {
        super("ClientSideException:" + message, cause);
    }
}
