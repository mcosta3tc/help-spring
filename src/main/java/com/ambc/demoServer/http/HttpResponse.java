package com.ambc.demoServer.http;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
@AllArgsConstructor
//Control the response to send back
public class HttpResponse {
    private Integer responseStatusCode;
    private HttpStatus responseHttpStatus;
    private String responseReasonMessage;
    private String responseDescription;
    /*
     *  Example
     *  {
     *      code (responseStatusCode) : 200,
     *      httpStatus (responseHttpStatus) : "OK",
     *      reason (responseReasonMessage) : "ok",
     *      message (responseDescription) : "Your request was successful"
     *  }
     */
}
