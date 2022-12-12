package se.magnus.util.http;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.ZonedDateTime;

@Getter
public class HttpErrorInfo {

    @JsonProperty("timestamp")
    private final ZonedDateTime timestamp;
    @JsonProperty("path")
    private final String path;
    @JsonProperty("httpStatus")
    private final HttpStatus httpStatus;
    @JsonProperty("message")
    private final String message;

    public HttpErrorInfo() {
        this.timestamp = null;
        this.path = null;
        this.httpStatus = null;
        this.message = null;
    }

    public HttpErrorInfo(HttpStatus httpStatus, String path, String message) {
        this.timestamp = ZonedDateTime.now();
        this.path = path;
        this.httpStatus = httpStatus;
        this.message = message;
    }
}
