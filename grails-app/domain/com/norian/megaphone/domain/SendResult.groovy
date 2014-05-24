package com.norian.megaphone.domain

class SendResult {

    Date timestamp
    ResultStatus status

    static constraints = {
    }
}

enum ResultStatus {
    Succeeded, Failed
}