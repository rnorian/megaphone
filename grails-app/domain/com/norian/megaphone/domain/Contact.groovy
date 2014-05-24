package com.norian.megaphone.domain

import grails.rest.*

@Resource(uri='/contacts', formats = ['json', 'xml'])
class Contact extends MessageTarget {
    String firstName;
    String lastName;
    String phoneNumber;

    static constraints = {
    }

    /*
    SendResult sendMessage(String pMessage) {
        SendResult result = new SendResult()
        result.status = ResultStatus.Succeeded
        result.timestamp = new Date()

        return result
    }
    */
}
