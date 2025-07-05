package com.course.practicaljava.api.response;

import java.time.ZonedDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;

/*
 * this class creates the object of an error message with its due
 * timestamp, thats it
 */

public class ErrorResponse {
    private String message;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ", timezone = "GMT+6")
    private ZonedDateTime timestamp;

    // empty constructor
    public ErrorResponse(){

    }

    public ErrorResponse(String message, ZonedDateTime timestamp){
        super();
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getMessage(){
        return message;
    }

    public ZonedDateTime getTimestamp(){
        return timestamp;
    }

    public void setMessage(String message){
        this.message = message;
    }

    public void setTimestamp(ZonedDateTime timestamp) {
        this.timestamp = timestamp;
    }
}