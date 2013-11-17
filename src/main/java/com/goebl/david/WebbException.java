package com.goebl.david;

/**
 * Runtime exception wrapping the real exception thrown by HttpUrlConnection et al.
 *
 * @author hgoebl
 */
public class WebbException extends RuntimeException {

    public WebbException(String message) {
        super(message);
    }

    public WebbException(String message, Throwable cause) {
        super(message, cause);
    }

    public WebbException(Throwable cause) {
        super(cause);
    }
}
