package com.norian.megaphone.domain

abstract class MessageTarget {

    String alias;

    static constraints = {
        alias nullable: true
    }

   abstract SendResult sendMessage(String pMessage);
}
