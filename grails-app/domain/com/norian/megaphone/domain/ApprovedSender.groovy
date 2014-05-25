package com.norian.megaphone.domain

class ApprovedSender {

    String name;
    String phoneNumber;

    static hasMany = [messages: Shout]

    static constraints = {
    }
}
