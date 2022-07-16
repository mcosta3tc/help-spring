package com.ambc.demoServer.httpResponse;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.util.Date;

@Getter
@Setter
//Control the response to send back
public class HttpResponseTemplate {
    private Integer responseStatusCode;
    private HttpStatus responseHttpStatus;
    private String responseReasonMessage;
    private String responseDescription;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "UTC")
    private Date executedAt;
    /*
     *  Example
     *  {
     *      code (responseStatusCode) : 200,
     *      httpStatus (responseHttpStatus) : "OK",
     *      reason (responseReasonMessage) : "ok",
     *      message (responseDescription) : "Your request was successful"
     *  }
     */

    public HttpResponseTemplate(Integer responseStatusCode, HttpStatus responseHttpStatus, String responseReasonMessage, String responseDescription) {
        this.responseStatusCode = responseStatusCode;
        this.responseHttpStatus = responseHttpStatus;
        this.responseReasonMessage = responseReasonMessage;
        this.responseDescription = responseDescription;
        this.executedAt = new Date();
    }

    public Date getExecutedAt() {
        return executedAt;
    }

    public void setExecutedAt(Date executedAt) {
        this.executedAt = new Date();
    }
}
